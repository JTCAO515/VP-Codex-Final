package space.go2china.visepanda.butler.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RouterAgentTest {
    private final RouterAgent router = new RouterAgent();

    @Test
    void classifiesCreateTripRequests() {
        assertThat(router.classify("Plan me a 5 day trip in China")).isEqualTo(ButlerIntent.CREATE_TRIP);
        assertThat(router.classify("I want to travel to Beijing and Shanghai")).isEqualTo(ButlerIntent.CREATE_TRIP);
    }

    @Test
    void classifiesAdjustTripRequests() {
        assertThat(router.classify("Make the Beijing day calmer")).isEqualTo(ButlerIntent.ADJUST_TRIP);
        assertThat(router.classify("This is too packed, lighten it")).isEqualTo(ButlerIntent.ADJUST_TRIP);
    }

    @Test
    void classifiesFactualQuestions() {
        assertThat(router.classify("Do I need a visa?")).isEqualTo(ButlerIntent.ASK_FACTUAL);
        assertThat(router.classify("How do I pay with Alipay?")).isEqualTo(ButlerIntent.ASK_FACTUAL);
    }

    @Test
    void classifiesRecommendationsConcernsLogisticsAndPreferences() {
        assertThat(router.classify("What's the best hotpot in Chengdu?")).isEqualTo(ButlerIntent.ASK_RECOMMENDATION);
        assertThat(router.classify("Is it safe to travel alone?")).isEqualTo(ButlerIntent.CONCERN);
        assertThat(router.classify("How do I get from Xi'an to Chengdu?")).isEqualTo(ButlerIntent.LOGISTICS);
        assertThat(router.classify("I'm a vegetarian and on a student budget")).isEqualTo(ButlerIntent.PREFERENCE_SIGNAL);
    }

    @Test
    void returnsUnclearForEmptyOrAmbiguousInput() {
        assertThat(router.classify("")).isEqualTo(ButlerIntent.UNCLEAR);
        assertThat(router.classify("hmm ok")).isEqualTo(ButlerIntent.UNCLEAR);
    }

    @Test
    void classifiesAcuteDistressMessagesAsConcern() {
        assertThat(router.classify("I was robbed near the station")).isEqualTo(ButlerIntent.CONCERN);
        assertThat(router.classify("someone stolen my wallet")).isEqualTo(ButlerIntent.CONCERN);
        assertThat(router.classify("I'm injured and need urgent help")).isEqualTo(ButlerIntent.CONCERN);
    }
}
