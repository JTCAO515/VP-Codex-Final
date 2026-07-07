package space.go2china.visepanda.butler.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LlmJsonRepairTest {
    private final LlmJsonRepair repair = new LlmJsonRepair(new ObjectMapper());

    @Test
    void extractsFencedJson() {
        JsonNode node = repair.parse("```json\n{\"intent\":\"create_trip\"}\n```");

        assertThat(node.get("intent").asText()).isEqualTo("create_trip");
    }

    @Test
    void repairsTruncatedStringAndBrackets() {
        JsonNode node = repair.parse("{\"assistantResponse\":{\"highlights\":[\"Beijing");

        assertThat(node.at("/assistantResponse/highlights/0").asText()).isEqualTo("Beijing");
    }

    @Test
    void findObjectEndFindsMatchingBraceForCompleteObject() {
        assertThat(repair.findObjectEnd("{\"a\":1}")).isEqualTo(7);
    }

    @Test
    void findObjectEndFindsMatchingBraceWithTrailingText() {
        String input = "{\"a\":[1,2]} and then some words, with a comma";
        assertThat(repair.findObjectEnd(input)).isEqualTo(11);
    }

    @Test
    void findObjectEndReturnsMinusOneForGenuineTruncation() {
        assertThat(repair.findObjectEnd("{\"a\":[1,2")).isEqualTo(-1);
    }

    @Test
    void findObjectEndIgnoresBracesInsideStrings() {
        String input = "{\"a\":\"looks like { an object [ or array\"}";
        assertThat(repair.findObjectEnd(input)).isEqualTo(input.length());
    }

    @Test
    void regressionCompleteObjectFollowedByChatterContainingCommaDoesNotCorruptInternalField() {
        String raw = "{\"intent\":\"create_trip\",\"assistantMessage\":\"Planned\",\"reason\":\"ok\",\"days\":["
                + "{\"day\":1,\"city\":\"Shanghai\",\"blocks\":[{\"time\":\"Morning\",\"title\":\"Bund\"}]},"
                + "{\"day\":2,\"city\":\"Shanghai\",\"blocks\":[{\"time\":\"Morning\",\"title\":\"Yu Garden\"}]}]}\n\n"
                + "Check for validity:\n - All quotes are straight double quotes, no trailing commas.\n"
                + " - Proper nesting.\n\n Yes, this is valid JSON. I return exactly this.";

        JsonNode node = repair.parse(raw);

        assertThat(node.at("/days/0/blocks/0/title").asText()).isEqualTo("Bund");
        assertThat(node.at("/days/1/blocks/0/title").asText()).isEqualTo("Yu Garden");
    }
}
