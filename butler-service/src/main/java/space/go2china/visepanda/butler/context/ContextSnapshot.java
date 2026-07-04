package space.go2china.visepanda.butler.context;

import java.util.List;
import space.go2china.visepanda.butler.memory.UserMemoryEntry;

public record ContextSnapshot(
        TravelStage stage,
        StageTemplate template,
        String currentCity,
        Integer currentDay,
        String entryPoint,
        String urgency,
        int readiness,
        List<String> gaps,
        List<UserMemoryEntry> profile
) {
}
