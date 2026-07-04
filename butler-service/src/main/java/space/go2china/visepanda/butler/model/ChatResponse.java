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
        return ok(intent, patch, suggestions, toolContext, "live", "Live Butler", "provider-chain", List.of("live"));
    }

    public static ChatResponse ok(String intent, CanvasPatch patch, List<String> suggestions, JsonNode toolContext,
                                  String mode, String modelLabel, String strategy, List<String> providersTried) {
        return new ChatResponse(true, mode, modelLabel, intent, strategy,
                providersTried, patch, suggestions, toolContext, null, null);
    }

    public static ChatResponse error(String error, String message) {
        return new ChatResponse(false, null, null, null, null, null, null, null, null, error, message);
    }
}
