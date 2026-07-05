package space.go2china.visepanda.butler.agent;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import space.go2china.visepanda.butler.context.ContextSnapshot;
import space.go2china.visepanda.butler.context.TravelStage;
import space.go2china.visepanda.butler.model.AssistantResponse;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.model.TripSummary;

@Component
public class Composer {
    public CanvasPatch compose(ExecutionPlan plan, ContextSnapshot context, List<AgentResult> results, CanvasPatch fallbackPatch, boolean timedOut) {
        CanvasPatch patch = results.stream().map(AgentResult::patch).filter(java.util.Objects::nonNull).findFirst().orElse(fallbackPatch);
        List<String> highlights = new ArrayList<>();
        results.forEach(result -> highlights.addAll(result.highlights()));
        if (highlights.isEmpty() && patch.assistantResponse() != null) highlights.addAll(patch.assistantResponse().highlights());
        String body = body(context, results, timedOut);
        AssistantResponse response = new AssistantResponse(
                headline(context, patch),
                body,
                highlights.stream().distinct().limit(5).toList(),
                timedOut ? "Some specialist steps timed out; I used the completed parts." : watchOut(results),
                nextStep(context),
                patch.assistantResponse() == null ? null : patch.assistantResponse().toolCards(),
                followUp(context),
                plan.emotionalTone()
        );
        return new CanvasPatch(patch.intent(), response.body(), response, patch.tripSummary(), patch.days(), patch.butlerAlerts(), patch.reason());
    }

    private String headline(ContextSnapshot context, CanvasPatch patch) {
        if (context.stage() == TravelStage.IN_CHINA) return "Do this next";
        if (patch.assistantResponse() != null) return patch.assistantResponse().headline();
        return "Plan updated";
    }

    private String body(ContextSnapshot context, List<AgentResult> results, boolean timedOut) {
        String joined = results.stream().map(AgentResult::summary).filter(s -> s != null && !s.isBlank()).reduce((a, b) -> a + " " + b).orElse("I kept this bounded and used fallback sources.");
        if (context.stage() == TravelStage.IN_CHINA && joined.length() > 240) joined = joined.substring(0, 240);
        if (timedOut) joined += " Some slower checks were skipped.";
        return joined;
    }

    private String watchOut(List<AgentResult> results) {
        boolean noSources = results.stream().allMatch(result -> result.sources().isEmpty());
        return noSources ? "I could not verify this with live sources; confirm before relying on it." : null;
    }

    private String nextStep(ContextSnapshot context) {
        if (context.stage() == TravelStage.IN_CHINA) return "Save the address and check the route now.";
        if (!context.gaps().isEmpty()) return "Fill the next missing trip detail.";
        return "Tell me what to refine next.";
    }

    private List<String> followUp(ContextSnapshot context) {
        if (!context.gaps().isEmpty()) return List.of(context.gaps().get(0), "Make the day lighter?");
        return List.of("Add food recommendations?", "Check payment and visa setup?");
    }
}
