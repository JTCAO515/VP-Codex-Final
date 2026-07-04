package space.go2china.visepanda.butler.persistence;

import space.go2china.visepanda.butler.memory.TripMemoryEntry;
import space.go2china.visepanda.butler.memory.UserMemoryEntry;

public interface MemoryPersistenceSink {
    void saveUserMemory(String userKey, UserMemoryEntry entry);

    void saveTripMemory(TripMemoryEntry entry);
}
