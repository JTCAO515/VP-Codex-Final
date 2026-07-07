package space.go2china.visepanda.butler.memory;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatMemory {
    private final int maxTurns;
    private final Map<String, SessionMemory> sessions = new ConcurrentHashMap<>();

    public ChatMemory(@Value("${butler.memory.window-rounds:12}") int windowRounds) {
        this.maxTurns = Math.max(2, windowRounds * 2);
    }

    public void addExchange(String sessionKey, String userMessage, String assistantMessage) {
        SessionMemory memory = sessions.computeIfAbsent(sessionKey, ignored -> new SessionMemory());
        synchronized (memory) {
            memory.turns.addLast(new ConversationTurn("user", userMessage, Instant.now()));
            memory.turns.addLast(new ConversationTurn("assistant", assistantMessage, Instant.now()));
            while (memory.turns.size() > maxTurns) {
                ConversationTurn old = memory.turns.removeFirst();
                String snippet = old.content() == null ? "" : old.content().replaceAll("\\s+", " ").trim();
                if (!snippet.isEmpty()) {
                    if (snippet.length() > 80) snippet = snippet.substring(0, 80);
                    memory.summary = appendSummary(memory.summary, old.role() + ": " + snippet);
                }
            }
        }
    }

    public ChatMemorySnapshot snapshot(String sessionKey) {
        SessionMemory memory = sessions.get(sessionKey);
        if (memory == null) return new ChatMemorySnapshot("", List.of());
        synchronized (memory) {
            return new ChatMemorySnapshot(memory.summary, List.copyOf(memory.turns));
        }
    }

    private String appendSummary(String summary, String entry) {
        List<String> parts = new ArrayList<>();
        if (summary != null && !summary.isBlank()) parts.add(summary);
        parts.add(entry);
        String joined = String.join(" | ", parts);
        return joined.length() <= 600 ? joined : joined.substring(joined.length() - 600);
    }

    private static class SessionMemory {
        private final ArrayDeque<ConversationTurn> turns = new ArrayDeque<>();
        private String summary = "";
    }
}
