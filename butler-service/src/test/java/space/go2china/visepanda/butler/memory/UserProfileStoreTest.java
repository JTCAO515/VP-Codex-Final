package space.go2china.visepanda.butler.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class UserProfileStoreTest {
    private final PreferenceSignalExtractor extractor = new PreferenceSignalExtractor();
    private final InMemoryUserProfileStore store = new InMemoryUserProfileStore(new MemoryPersistenceNoop());

    @Test
    void writesExplicitVegetarianPreferenceAtHighConfidence() {
        store.applySignals("guest:g1", extractor.extract("I am vegetarian"));

        List<UserMemoryEntry> memories = store.topK("guest:g1", "create_trip", 6);
        assertThat(memories).anySatisfy(entry -> {
            assertThat(entry.key()).isEqualTo("dietary");
            assertThat(entry.value()).isEqualTo("vegetarian");
            assertThat(entry.confidence()).isGreaterThanOrEqualTo(0.9);
            assertThat(entry.source()).isEqualTo("explicit");
        });
    }

    @Test
    void upgradesImplicitVegetarianSignalAfterThreeEvidenceItems() {
        store.applySignals("guest:g2", extractor.extract("Any vegetarian restaurant near the hotel?"));
        store.applySignals("guest:g2", extractor.extract("Find vegetarian food for day 2"));
        store.applySignals("guest:g2", extractor.extract("Need a 素食餐厅 in Beijing"));

        UserMemoryEntry entry = store.topK("guest:g2", "food", 6).get(0);
        assertThat(entry.value()).isEqualTo("vegetarian");
        assertThat(entry.confidence()).isGreaterThanOrEqualTo(0.75);
        assertThat(entry.evidence()).hasSize(3);
    }

    @Test
    void correctionClearsOldPreferenceButKeepsEvidence() {
        store.applySignals("guest:g3", extractor.extract("I am vegetarian"));
        store.applySignals("guest:g3", extractor.extract("I am now not vegetarian"));

        assertThat(store.topK("guest:g3", "food", 6)).isEmpty();
    }

    @Test
    void listAllExcludesCorrectedAwayEntries() {
        store.applySignals("guest:g5", extractor.extract("I am vegetarian"));

        assertThat(store.listAll("guest:g5")).hasSize(1);

        store.applySignals("guest:g5", extractor.extract("I am now not vegetarian"));

        assertThat(store.listAll("guest:g5")).isEmpty();
    }

    @Test
    void deleteRemovesExactEntryAndReportsWhetherSomethingWasRemoved() {
        store.applySignals("guest:g6", extractor.extract("I am vegetarian"));

        assertThat(store.delete("guest:g6", "dietary", "not-a-real-value")).isFalse();
        assertThat(store.listAll("guest:g6")).hasSize(1);

        assertThat(store.delete("guest:g6", "dietary", "vegetarian")).isTrue();
        assertThat(store.listAll("guest:g6")).isEmpty();
    }

    @Test
    void migratesGuestProfileToUserProfile() {
        store.applySignals("guest:g4", extractor.extract("I am vegetarian"));

        store.migrateGuestToUser("guest:g4", "user:u4");

        assertThat(store.topK("guest:g4", "food", 6)).isEmpty();
        assertThat(store.topK("user:u4", "food", 6)).isNotEmpty();
    }
}
