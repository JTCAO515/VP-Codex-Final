package space.go2china.visepanda.butler.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import space.go2china.visepanda.butler.agent.ExecutionPlan;
import space.go2china.visepanda.butler.memory.MemoryContext;
import space.go2china.visepanda.butler.model.ChatRequest;
import space.go2china.visepanda.butler.model.TripState;
import space.go2china.visepanda.butler.model.TripSummary;

class ContextInjectionServiceTest {
    @Test
    void switchesToInChinaTemplateWhenCurrentCityPresent() {
        TripState trip = new TripState(new TripSummary("Trip", 1, "Balanced", "Solo", List.of("Beijing"), "Draft"), List.of(), List.of(), "");
        ChatRequest request = new ChatRequest("help", trip, null, null, "s1", "g1", null, null, "Beijing", 1, "chat", null);
        MemoryContext memory = new MemoryContext("guest:g1", "s1", "s1", "", List.of(), List.of(), List.of());
        ExecutionPlan plan = new ExecutionPlan(List.of(), "single", "neutral", "normal", false);

        ContextSnapshot snapshot = new ContextInjectionService().build(request, memory, plan);

        assertThat(snapshot.stage()).isEqualTo(TravelStage.IN_CHINA);
        assertThat(snapshot.template().id()).isEqualTo("IN_CHINA_MODE");
    }
}
