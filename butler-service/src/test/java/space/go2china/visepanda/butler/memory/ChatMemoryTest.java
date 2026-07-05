package space.go2china.visepanda.butler.memory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChatMemoryTest {
    @Test
    void keepsSlidingWindowAndSummarizesOverflow() {
        ChatMemory memory = new ChatMemory(2);

        memory.addExchange("s1", "I am vegetarian", "Noted");
        memory.addExchange("s1", "Plan Beijing", "Drafted");
        memory.addExchange("s1", "Make it cheaper", "Adjusted");

        ChatMemorySnapshot snapshot = memory.snapshot("s1");
        assertThat(snapshot.recentTurns()).hasSize(4);
        assertThat(snapshot.runningSummary()).contains("I am vegetarian");
        assertThat(snapshot.recentTurns().get(0).content()).isEqualTo("Plan Beijing");
    }
}
