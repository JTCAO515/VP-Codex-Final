package space.go2china.visepanda.butler.agent;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.model.ChatResponse;
import space.go2china.visepanda.butler.model.TripState;

@Service
public class TripPlannerAgent {
    private final MockButlerService mockButler;
    private final String apiKey;

    public TripPlannerAgent(MockButlerService mockButler, @Value("${butler.llm.api-key:}") String apiKey) {
        this.mockButler = mockButler;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public ChatResponse plan(ButlerIntent routedIntent, String message, TripState trip) {
        CanvasPatch patch = mockButler.createPatch(message, trip);
        return ChatResponse.ok(routedIntent.wireName(), patch, suggestionsFor(patch.intent()));
    }

    public boolean liveConfigured() {
        return !apiKey.isBlank();
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
