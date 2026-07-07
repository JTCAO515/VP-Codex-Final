package space.go2china.visepanda.butler.agent;

import java.util.List;

public record ExecutionPlan(List<ExecutionStep> steps, String composition, String emotionalTone, String urgency, boolean ambiguous) {
    public ExecutionPlan {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }
}
