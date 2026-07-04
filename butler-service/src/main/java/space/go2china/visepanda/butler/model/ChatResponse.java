package space.go2china.visepanda.butler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatResponse(
        boolean ok,
        String mode,
        String modelLabel,
        String intent,
        String strategy,
        List<String> providersTried,
        CanvasPatch patch,
        List<String> suggestions,
        JsonNode toolContext,
        String error,
        String message
) {
    public static ChatResponse ok(String intent, CanvasPatch patch, List<String> suggestions) {
        return ok(intent, patch, suggestions, null);
    }

    public static ChatResponse ok(String intent, CanvasPatch patch, List<String> suggestions, JsonNode toolContext) {
        return new ChatResponse(true, "mock", "Mock Butler", intent, "mock-fallback",
                List.of("mock"), patch, suggestions, toolContext, null, null);
    }

    public static ChatResponse error(String error, String message) {
        return new ChatResponse(false, null, null, null, null, null, null, null, null, error, message);
    }
}
