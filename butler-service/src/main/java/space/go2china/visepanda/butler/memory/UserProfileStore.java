package space.go2china.visepanda.butler.memory;

import java.util.List;

public interface UserProfileStore {
    void applySignals(String userKey, List<PreferenceSignal> signals);

    List<UserMemoryEntry> topK(String userKey, String topic, int limit);

    void migrateGuestToUser(String guestKey, String userKey);
}
