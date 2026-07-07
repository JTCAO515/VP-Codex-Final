package space.go2china.visepanda.butler.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CanvasPatchNormalizer {
    private final ObjectMapper objectMapper;

    CanvasPatchNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    JsonNode normalize(JsonNode root) {
        if (!(root instanceof ObjectNode object)) return root;

        array(object, "days");
        array(object, "butlerAlerts");
        object(object.get("tripSummary"), summary -> array(summary, "destinations"));
        object(object.get("assistantResponse"), response -> {
            array(response, "highlights");
            array(response, "toolCards");
            array(response, "followUp");
            each(response.get("toolCards"), card -> array(card, "items"));
        });
        each(object.get("days"), day -> {
            array(day, "blocks");
            array(day, "food");
            each(day.get("blocks"), block -> {
                array(block, "highlights");
                array(block, "bookingCandidates");
            });
        });
        return object;
    }

    private void array(ObjectNode object, String field) {
        JsonNode value = object.get(field);
        if (value == null || value.isNull()) {
            object.set(field, objectMapper.createArrayNode());
            return;
        }
        if (!value.isArray()) {
            object.set(field, objectMapper.createArrayNode().add(value));
        }
    }

    private void each(JsonNode node, java.util.function.Consumer<ObjectNode> consumer) {
        if (!(node instanceof ArrayNode array)) return;
        array.forEach(item -> object(item, consumer));
    }

    private void object(JsonNode node, java.util.function.Consumer<ObjectNode> consumer) {
        if (node instanceof ObjectNode object) consumer.accept(object);
    }
}
