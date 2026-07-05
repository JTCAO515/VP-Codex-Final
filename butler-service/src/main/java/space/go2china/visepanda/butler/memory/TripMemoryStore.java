package space.go2china.visepanda.butler.memory;

import java.util.List;

public interface TripMemoryStore {
    void record(String tripKey, String kind, String body);

    List<TripMemoryEntry> recent(String tripKey, int limit);

    void migrateGuestToUser(String guestTripKey, String userTripKey);
}
