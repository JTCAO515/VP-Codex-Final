package space.go2china.visepanda.butler.memory;

import java.util.List;

public record ChatMemorySnapshot(String runningSummary, List<ConversationTurn> recentTurns) {
}
