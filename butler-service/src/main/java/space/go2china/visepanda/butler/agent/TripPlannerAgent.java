package space.go2china.visepanda.butler.agent;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.context.ContextSnapshot;
import space.go2china.visepanda.butler.memory.MemoryContext;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.model.ChatResponse;
import space.go2china.visepanda.butler.model.TripState;
import space.go2china.visepanda.butler.tools.ToolBudget;

@Service
public class TripPlannerAgent {
    private final OpenAiCompatibleLlmClient llmClient;
    private final LlmJsonRepair jsonRepair;
    private final CanvasPatchNormalizer canvasPatchNormalizer;
    private final ObjectMapper objectMapper;

    public TripPlannerAgent(OpenAiCompatibleLlmClient llmClient, LlmJsonRepair jsonRepair, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.jsonRepair = jsonRepair;
        this.canvasPatchNormalizer = new CanvasPatchNormalizer(objectMapper);
        this.objectMapper = objectMapper;
    }

    public ChatResponse plan(ButlerIntent routedIntent, String message, TripState trip) {
        return plan(routedIntent, message, trip, null, null);
    }

    public ChatResponse plan(ButlerIntent routedIntent, String message, TripState trip, MemoryContext memoryContext,
                             com.fasterxml.jackson.databind.JsonNode toolContext) {
        AgentResult result = run(routedIntent, new ExecutionStep("TripPlannerAgent", message, List.of()), message, trip,
                memoryContext, null, new ToolBudget(0));
        return ChatResponse.ok(routedIntent.wireName(), result.patch(), suggestionsFor(result.patch().intent()), toolContext,
                result.mode(), result.modelLabel(), "provider-chain", result.providersTried());
    }

    public AgentResult run(ButlerIntent routedIntent, ExecutionStep step, String message, TripState trip, MemoryContext memory,
                           ContextSnapshot context, ToolBudget budget) {
        LlmCompletion completion = llmClient.completeJson(systemPrompt(), userPrompt(routedIntent, step, message, trip, memory, context));
        CanvasPatch patch = parsePatch(completion.content());
        return new AgentResult("TripPlannerAgent", patch.assistantMessage(), patch,
                patch.assistantResponse() == null ? List.of() : patch.assistantResponse().highlights(),
                List.of(completion.modelLabel()), false, completion.providerId(), completion.modelLabel(),
                List.of(completion.providerId()));
    }

    private CanvasPatch parsePatch(String content) {
        try {
            return objectMapper.treeToValue(canvasPatchNormalizer.normalize(jsonRepair.parse(content)), CanvasPatch.class);
        } catch (Exception error) {
            throw new LlmUnavailableException("Model output could not be parsed as CanvasPatch: " + error.getMessage());
        }
    }

    private String systemPrompt() {
        return """
                You are VisePanda's China travel butler. Return only JSON matching CanvasPatch.
                Required top-level fields: intent, assistantMessage, assistantResponse, reason.
                For itinerary changes include tripSummary and a complete days array. Never invent official booking availability.
                assistantResponse must include headline, body, highlights, watchOut, nextStep, followUp.
                Keep output concise and valid JSON.
                """;
    }

    private String userPrompt(ButlerIntent routedIntent, ExecutionStep step, String message, TripState trip, MemoryContext memory,
                              ContextSnapshot context) {
        return """
                Intent: %s
                Task: %s
                User message: %s
                Current trip JSON: %s
                Memory: %s
                Context: %s
                """.formatted(
                routedIntent.wireName(),
                step == null ? message : step.task(),
                message,
                write(trip),
                write(memory),
                write(context)
        );
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return "{}";
        }
    }

    private List<String> suggestionsFor(String patchIntent) {
        if ("create_trip".equals(patchIntent)) {
            return List.of("Make this trip less tiring", "Add payment and visa reminders", "Where should I stay?");
        }
        if ("add_alerts".equals(patchIntent)) {
            return List.of("Review payment setup", "Check visa requirements", "Prepare translation phrases");
        }
        return List.of("Make it more relaxed", "Add food recommendations", "Update hotel areas");
    }
}
