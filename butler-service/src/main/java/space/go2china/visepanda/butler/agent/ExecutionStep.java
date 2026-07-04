package space.go2china.visepanda.butler.agent;

import java.util.List;

public record ExecutionStep(String agent, String task, List<Integer> dependsOn) {
    public ExecutionStep {
        dependsOn = dependsOn == null ? List.of() : List.copyOf(dependsOn);
    }
}
