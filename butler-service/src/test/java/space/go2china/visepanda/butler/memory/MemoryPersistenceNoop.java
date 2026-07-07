package space.go2china.visepanda.butler.memory;

import space.go2china.visepanda.butler.persistence.MemoryPersistenceSink;

class MemoryPersistenceNoop implements MemoryPersistenceSink {
    @Override
    public void saveUserMemory(String userKey, UserMemoryEntry entry) {
    }

    @Override
    public void saveTripMemory(TripMemoryEntry entry) {
    }

    @Override
    public void deleteUserMemory(String userKey, String entryKey, String entryValue) {
    }
}
