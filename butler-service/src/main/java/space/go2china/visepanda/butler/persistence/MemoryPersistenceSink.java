package space.go2china.visepanda.butler.persistence;

import space.go2china.visepanda.butler.memory.TripMemoryEntry;
import space.go2china.visepanda.butler.memory.UserMemoryEntry;

public interface MemoryPersistenceSink {
    void saveUserMemory(String userKey, UserMemoryEntry entry);

    void saveTripMemory(TripMemoryEntry entry);

    /** Durable delete counterpart to saveUserMemory — must be called on every delete so a
     * later cold-start rehydration from Supabase cannot resurrect a "deleted" entry. */
    void deleteUserMemory(String userKey, String entryKey, String entryValue);
}
