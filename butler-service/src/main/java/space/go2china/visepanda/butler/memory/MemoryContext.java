package space.go2china.visepanda.butler.memory;

import java.util.List;

public record MemoryContext(
        String userKey,
        String sessionKey,
        String tripKey,
        String runningSummary,
        List<ConversationTurn> recentTurns,
        List<UserMemoryEntry> profile,
        List<TripMemoryEntry> tripMemories
) {
}
