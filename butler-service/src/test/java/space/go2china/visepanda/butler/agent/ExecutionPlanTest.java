package space.go2china.visepanda.butler.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExecutionPlanTest {
    private final RouterAgent router = new RouterAgent();

    @Test
    void simpleTripAdjustmentIsSingleStep() {
        ExecutionPlan plan = router.plan("Make Day 2 lighter");

        assertThat(plan.steps()).hasSize(1);
        assertThat(plan.steps().get(0).agent()).isEqualTo("TripPlannerAgent");
        assertThat(plan.composition()).isEqualTo("single");
    }

    @Test
    void compoundTripAndLunchRequestSplitsSteps() {
        ExecutionPlan plan = router.plan("Make Day 2 lighter and recommend nearby lunch");

        assertThat(plan.steps()).extracting(ExecutionStep::agent)
                .containsExactly("TripPlannerAgent", "LocalExpertAgent");
        assertThat(plan.composition()).isEqualTo("merge");
    }
}
