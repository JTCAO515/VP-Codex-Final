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
}
