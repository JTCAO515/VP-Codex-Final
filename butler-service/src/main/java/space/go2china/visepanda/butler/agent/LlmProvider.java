package space.go2china.visepanda.butler.agent;

import java.util.Map;

record LlmProvider(
        String id,
        String label,
        String apiKey,
        String baseUrl,
        String model,
        Map<String, Object> extraBody
) {
}
