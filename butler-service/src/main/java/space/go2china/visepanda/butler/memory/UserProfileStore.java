package space.go2china.visepanda.butler.memory;

import java.util.List;

public interface UserProfileStore {
    void applySignals(String userKey, List<PreferenceSignal> signals);

    List<UserMemoryEntry> topK(String userKey, String topic, int limit);

    /** All entries for a user, newest first — for the "view your profile" surface (Issue #14). */
    List<UserMemoryEntry> listAll(String userKey);

    /**
     * Removes a single entry. Entries are internally keyed by (key, value) —
     * see InMemoryUserProfileStore.applySignals — since a user can hold more
     * than one entry under the same key (e.g. multiple "interest" values), so
     * both must be supplied to unambiguously identify one entry.
     * Returns true if an entry was actually removed.
     */
    boolean delete(String userKey, String entryKey, String entryValue);

    void migrateGuestToUser(String guestKey, String userKey);
}
