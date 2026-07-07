package space.go2china.visepanda.butler.memory;

import java.time.Instant;
import java.util.List;

public record UserMemoryEntry(
        String key,
        String value,
        double confidence,
        List<String> evidence,
        String source,
        Instant updatedAt
) {
    public UserMemoryEntry withEvidence(String nextEvidence, double nextConfidence, String nextSource) {
        List<String> merged = new java.util.ArrayList<>(evidence == null ? List.of() : evidence);
        if (nextEvidence != null && !nextEvidence.isBlank()) merged.add(nextEvidence);
        if (merged.size() > 8) merged = merged.subList(merged.size() - 8, merged.size());
        return new UserMemoryEntry(key, value, Math.min(1.0, nextConfidence), List.copyOf(merged), nextSource, Instant.now());
    }
}
