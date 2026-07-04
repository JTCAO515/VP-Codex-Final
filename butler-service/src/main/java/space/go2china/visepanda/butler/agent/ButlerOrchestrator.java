package space.go2china.visepanda.butler.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.context.ContextInjectionService;
import space.go2china.visepanda.butler.context.ContextSnapshot;
import space.go2china.visepanda.butler.memory.MemoryContext;
import space.go2china.visepanda.butler.memory.MemoryContextService;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.model.ChatRequest;
import space.go2china.visepanda.butler.model.ChatResponse;
import space.go2china.visepanda.butler.tools.ToolBudget;

@Service
public class ButlerOrchestrator {
    static final int MAX_DEPTH = 2;
    static final int MAX_TOOL_CALLS = 6;
    static final Duration TOTAL_BUDGET = Duration.ofSeconds(25);

    private final RouterAgent routerAgent;
    private final MemoryContextService memoryContextService;
    private final ContextInjectionService contextInjectionService;
    private final TripPlannerAgent tripPlannerAgent;
    private final LocalExpertAgent localExpertAgent;
    private final LogisticsAgent logisticsAgent;
    private final TranslatorAgent translatorAgent;
    private final Composer composer;
    private final ObjectMapper objectMapper;

    public ButlerOrchestrator(RouterAgent routerAgent, MemoryContextService memoryContextService,
                              ContextInjectionService contextInjectionService, TripPlannerAgent tripPlannerAgent,
                              LocalExpertAgent localExpertAgent, LogisticsAgent logisticsAgent,
                              TranslatorAgent translatorAgent, Composer composer, ObjectMapper objectMapper) {
        this.routerAgent = routerAgent;
        this.memoryContextService = memoryContextService;
        this.contextInjectionService = contextInjectionService;
        this.tripPlannerAgent = tripPlannerAgent;
        this.localExpertAgent = localExpertAgent;
        this.logisticsAgent = logisticsAgent;
        this.translatorAgent = translatorAgent;
        this.composer = composer;
        this.objectMapper = objectMapper;
    }

    public OrchestrationResponse run(ChatRequest request) {
        ButlerIntent intent = routerAgent.classify(request.message());
        ExecutionPlan plan = routerAgent.plan(request.message());
        MemoryContext memory = memoryContextService.build(request, intent);
        ContextSnapshot context = contextInjectionService.build(request, memory, plan);
        ToolBudget budget = new ToolBudget(MAX_TOOL_CALLS);
        Instant start = Instant.now();
        List<AgentResult> results = new ArrayList<>();
        List<AgentExecutionLog> logs = new ArrayList<>();
        boolean timedOut = false;

        for (ExecutionStep step : plan.steps().stream().limit(MAX_DEPTH).toList()) {
            if (Duration.between(start, Instant.now()).compareTo(TOTAL_BUDGET) > 0) {
                timedOut = true;
                break;
            }
            Instant stepStart = Instant.now();
            int beforeTools = budget.calls();
            AgentResult result = runStep(intent, step, request, memory, context, budget);
            results.add(result);
            logs.add(new AgentExecutionLog(step.agent(), step.task(), Duration.between(stepStart, Instant.now()).toMillis(),
                    result.partial(), budget.calls() - beforeTools));
        }

        if (plan.steps().size() > MAX_DEPTH) timedOut = true;
        CanvasPatch fallback = tripPlannerAgent.run(intent, new ExecutionStep("TripPlannerAgent", request.message(), List.of()),
                request.message(), request.trip(), memory, context, budget).patch();
        CanvasPatch patch = composer.compose(plan, context, results, fallback, timedOut);
        JsonNode toolContext = objectMapper.valueToTree(Map.of(
                "executionPlan", plan,
                "executionLog", logs,
                "context", context,
                "toolCalls", budget.calls(),
                "maxToolCalls", budget.maxCalls(),
                "maxDepth", MAX_DEPTH,
                "timedOut", timedOut
        ));
        ChatResponse response = ChatResponse.ok(intent.wireName(), patch, patch.assistantResponse().followUp(), toolContext);
        return new OrchestrationResponse(response, memory);
    }

    private AgentResult runStep(ButlerIntent intent, ExecutionStep step, ChatRequest request, MemoryContext memory,
                                ContextSnapshot context, ToolBudget budget) {
        return switch (step.agent()) {
            case "LocalExpertAgent" -> localExpertAgent.run(step, request.message(), memory, context, budget);
            case "LogisticsAgent" -> logisticsAgent.run(step, request.message(), memory, context, budget);
            case "TranslatorAgent" -> translatorAgent.run(step, request.message(), memory, context, budget);
            default -> tripPlannerAgent.run(intent, step, request.message(), request.trip(), memory, context, budget);
        };
    }

    public record OrchestrationResponse(ChatResponse response, MemoryContext memoryContext) {
    }
}
