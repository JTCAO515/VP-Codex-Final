package space.go2china.visepanda.butler.memory;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import space.go2china.visepanda.butler.persistence.MemoryPersistenceSink;

@Component
public class InMemoryTripMemoryStore implements TripMemoryStore {
    private final Map<String, ArrayDeque<TripMemoryEntry>> entries = new ConcurrentHashMap<>();
    private final MemoryPersistenceSink persistenceSink;

    public InMemoryTripMemoryStore(MemoryPersistenceSink persistenceSink) {
        this.persistenceSink = persistenceSink;
    }

    @Override
    public void record(String tripKey, String kind, String body) {
        if (body == null || body.isBlank()) return;
        ArrayDeque<TripMemoryEntry> queue = entries.computeIfAbsent(tripKey, ignored -> new ArrayDeque<>());
        synchronized (queue) {
            TripMemoryEntry entry = new TripMemoryEntry(tripKey, kind, body, Instant.now());
            queue.addLast(entry);
            while (queue.size() > 50) queue.removeFirst();
            persistenceSink.saveTripMemory(entry);
        }
    }

    @Override
    public List<TripMemoryEntry> recent(String tripKey, int limit) {
        ArrayDeque<TripMemoryEntry> queue = entries.get(tripKey);
        if (queue == null) return List.of();
        synchronized (queue) {
            ArrayList<TripMemoryEntry> copy = new ArrayList<>(queue);
            int from = Math.max(0, copy.size() - limit);
            return List.copyOf(copy.subList(from, copy.size()));
        }
    }

    @Override
    public void migrateGuestToUser(String guestTripKey, String userTripKey) {
        ArrayDeque<TripMemoryEntry> guest = entries.remove(guestTripKey);
        if (guest == null) return;
        ArrayDeque<TripMemoryEntry> user = entries.computeIfAbsent(userTripKey, ignored -> new ArrayDeque<>());
        synchronized (user) {
            user.addAll(guest);
        }
    }
}
