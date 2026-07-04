package space.go2china.visepanda.butler.agent;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.context.ContextSnapshot;
import space.go2china.visepanda.butler.memory.MemoryContext;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.model.ChatResponse;
import space.go2china.visepanda.butler.model.TripState;
import space.go2china.visepanda.butler.tools.ToolBudget;

@Service
public class TripPlannerAgent {
    private final MockButlerService mockButler;
    private final String apiKey;

    public TripPlannerAgent(MockButlerService mockButler, @Value("${butler.llm.api-key:}") String apiKey) {
        this.mockButler = mockButler;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public ChatResponse plan(ButlerIntent routedIntent, String message, TripState trip) {
        return plan(routedIntent, message, trip, null, null);
    }

    public ChatResponse plan(ButlerIntent routedIntent, String message, TripState trip, MemoryContext memoryContext,
                             com.fasterxml.jackson.databind.JsonNode toolContext) {
        CanvasPatch patch = mockButler.createPatch(message, trip);
        return ChatResponse.ok(routedIntent.wireName(), patch, suggestionsFor(patch.intent()), toolContext);
    }

    public boolean liveConfigured() {
        return !apiKey.isBlank();
    }

    public AgentResult run(ButlerIntent routedIntent, ExecutionStep step, String message, TripState trip, MemoryContext memory,
                           ContextSnapshot context, ToolBudget budget) {
        CanvasPatch patch = mockButler.createPatch(message, trip);
        return new AgentResult("TripPlannerAgent", patch.assistantMessage(), patch,
                patch.assistantResponse() == null ? List.of() : patch.assistantResponse().highlights(), List.of("Mock Butler"), false);
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
