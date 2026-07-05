package space.go2china.visepanda.butler.memory;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import space.go2china.visepanda.butler.persistence.MemoryPersistenceSink;

@Component
public class InMemoryUserProfileStore implements UserProfileStore {
    private final Map<String, Map<String, UserMemoryEntry>> profiles = new ConcurrentHashMap<>();
    private final MemoryPersistenceSink persistenceSink;

    public InMemoryUserProfileStore(MemoryPersistenceSink persistenceSink) {
        this.persistenceSink = persistenceSink;
    }

    @Override
    public void applySignals(String userKey, List<PreferenceSignal> signals) {
        Map<String, UserMemoryEntry> profile = profiles.computeIfAbsent(userKey, ignored -> new ConcurrentHashMap<>());
        for (PreferenceSignal signal : signals) {
            String id = signal.key() + ":" + signal.value();
            UserMemoryEntry existing = profile.get(id);
            if (signal.correction()) {
                UserMemoryEntry entry = new UserMemoryEntry(signal.key(), signal.value(), 0.0,
                        append(existing, "Correction: " + signal.evidence()), "explicit", Instant.now());
                profile.put(id, entry);
                persistenceSink.saveUserMemory(userKey, entry);
                continue;
            }
            if (existing == null) {
                UserMemoryEntry entry = new UserMemoryEntry(signal.key(), signal.value(), signal.confidence(),
                        List.of(signal.evidence()), signal.source(), Instant.now());
                profile.put(id, entry);
                persistenceSink.saveUserMemory(userKey, entry);
                continue;
            }
            int evidenceCount = (existing.evidence() == null ? 0 : existing.evidence().size()) + 1;
            double confidence = Math.max(existing.confidence(), signal.confidence());
            String source = "explicit".equals(existing.source()) || "explicit".equals(signal.source()) ? "explicit" : "inferred";
            if (!"explicit".equals(source) && evidenceCount >= 3) confidence = Math.max(confidence, 0.75);
            UserMemoryEntry entry = existing.withEvidence(signal.evidence(), confidence, source);
            profile.put(id, entry);
            persistenceSink.saveUserMemory(userKey, entry);
        }
    }

    @Override
    public List<UserMemoryEntry> topK(String userKey, String topic, int limit) {
        Map<String, UserMemoryEntry> profile = profiles.getOrDefault(userKey, Map.of());
        return profile.values().stream()
                .filter(entry -> entry.confidence() > 0)
                .sorted(Comparator.comparingDouble(UserMemoryEntry::confidence).reversed()
                        .thenComparing(UserMemoryEntry::updatedAt, Comparator.reverseOrder()))
                .limit(limit)
                .toList();
    }

    @Override
    public List<UserMemoryEntry> listAll(String userKey) {
        Map<String, UserMemoryEntry> profile = profiles.getOrDefault(userKey, Map.of());
        // Same confidence(>0) filter as topK: a corrected-away preference is kept at
        // confidence 0 only so its evidence trail survives internally — showing it on
        // the "your profile" surface would look like a preference the user still has,
        // right after they explicitly said otherwise.
        return profile.values().stream()
                .filter(entry -> entry.confidence() > 0)
                .sorted(Comparator.comparing(UserMemoryEntry::updatedAt).reversed())
                .toList();
    }

    @Override
    public boolean delete(String userKey, String entryKey, String entryValue) {
        Map<String, UserMemoryEntry> profile = profiles.get(userKey);
        if (profile == null) return false;
        boolean removed = profile.remove(entryKey + ":" + entryValue) != null;
        if (removed) persistenceSink.deleteUserMemory(userKey, entryKey, entryValue);
        return removed;
    }

    @Override
    public void migrateGuestToUser(String guestKey, String userKey) {
        Map<String, UserMemoryEntry> guest = profiles.get(guestKey);
        if (guest == null || guest.isEmpty()) return;
        Map<String, UserMemoryEntry> user = profiles.computeIfAbsent(userKey, ignored -> new ConcurrentHashMap<>());
        user.putAll(new LinkedHashMap<>(guest));
        profiles.remove(guestKey);
    }

    private List<String> append(UserMemoryEntry existing, String evidence) {
        if (existing == null || existing.evidence() == null) return List.of(evidence);
        java.util.ArrayList<String> copy = new java.util.ArrayList<>(existing.evidence());
        copy.add(evidence);
        return List.copyOf(copy);
    }
}
