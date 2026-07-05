package space.go2china.visepanda.butler.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OpenAiCompatibleLlmClient {
    private static final int DEFAULT_TIMEOUT_MS = 18_000;
    private static final int DEFAULT_MAX_TOKENS = 2_200;
    private final LlmProviderRegistry registry;
    private final LlmHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleLlmClient(LlmProviderRegistry registry, LlmHttpClient httpClient, ObjectMapper objectMapper) {
        this.registry = registry;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public LlmCompletion completeJson(String systemPrompt, String userPrompt) {
        List<LlmProvider> providers = registry.configuredProviders();
        if (providers.isEmpty()) {
            throw new LlmUnavailableException("No Chinese LLM provider is configured.");
        }

        List<String> failures = new ArrayList<>();
        for (LlmProvider provider : providers) {
            try {
                String body = objectMapper.writeValueAsString(body(provider, systemPrompt, userPrompt));
                Duration timeout = Duration.ofMillis(Math.max(DEFAULT_TIMEOUT_MS, provider.minTimeoutMs()));
                LlmHttpResponse response = httpClient.postJson(provider.baseUrl() + "/chat/completions",
                        provider.apiKey(), body, timeout);
                if (!response.ok()) {
                    throw new LlmUnavailableException(provider.label() + ": HTTP " + response.status());
                }
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode content = root.at("/choices/0/message/content");
                if (!content.isTextual() || content.asText().isBlank()) {
                    throw new LlmUnavailableException(provider.label() + ": response did not include message content.");
                }
                return new LlmCompletion(provider.id(), provider.label(), provider.model(), content.asText());
            } catch (Exception error) {
                failures.add(provider.id() + ": " + error.getMessage());
            }
        }
        throw new LlmUnavailableException("All Chinese LLM providers failed: " + String.join("; ", failures));
    }

    private Map<String, Object> body(LlmProvider provider, String systemPrompt, String userPrompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", provider.model());
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("max_tokens", Math.max(DEFAULT_MAX_TOKENS, provider.minMaxTokens()));
        body.put("temperature", 0.3);
        body.put("response_format", Map.of("type", "json_object"));
        body.putAll(provider.extraBody());
        return body;
    }
}
