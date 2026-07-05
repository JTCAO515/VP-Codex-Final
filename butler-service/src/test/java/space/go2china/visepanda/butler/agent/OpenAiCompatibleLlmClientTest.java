package space.go2china.visepanda.butler.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class OpenAiCompatibleLlmClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendsProviderSpecificBodyFields() {
        CapturingHttpClient http = new CapturingHttpClient(200, response("ok"));
        MockEnvironment env = new MockEnvironment().withProperty("DEEPSEEK_API_KEY", "deepseek-key");
        OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(new LlmProviderRegistry(env), http, objectMapper);

        LlmCompletion completion = client.completeJson("system", "user");

        assertThat(completion.providerId()).isEqualTo("deepseek");
        assertThat(http.urls.get(0)).isEqualTo("https://api.deepseek.com/chat/completions");
        assertThat(http.bodies.get(0)).contains("\"response_format\":{\"type\":\"json_object\"}");
        assertThat(http.bodies.get(0)).contains("\"thinking\":{\"type\":\"disabled\"}");
    }

    @Test
    void fallsThroughProviderChainWithoutMocking() {
        CapturingHttpClient http = new CapturingHttpClient(500, "bad", 200, response("ok"));
        MockEnvironment env = new MockEnvironment()
                .withProperty("DASHSCOPE_API_KEY", "qwen-key")
                .withProperty("ZHIPU_API_KEY", "zhipu-key");
        OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(new LlmProviderRegistry(env), http, objectMapper);

        LlmCompletion completion = client.completeJson("system", "user");

        assertThat(completion.providerId()).isEqualTo("zhipu");
        assertThat(http.bodies.get(1)).contains("\"thinking\":{\"type\":\"disabled\"}");
    }

    @Test
    void kimiTemperatureIsAlwaysOne() {
        CapturingHttpClient http = new CapturingHttpClient(200, response("ok"));
        MockEnvironment env = new MockEnvironment().withProperty("MOONSHOT_API_KEY", "kimi-key");
        OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(new LlmProviderRegistry(env), http, objectMapper);

        client.completeJson("system", "user");

        assertThat(http.bodies.get(0)).contains("\"temperature\":1");
    }

    @Test
    void kimiUsesProviderTimeoutAndTokenFloors() {
        CapturingHttpClient http = new CapturingHttpClient(200, response("ok"));
        MockEnvironment env = new MockEnvironment().withProperty("MOONSHOT_API_KEY", "kimi-key");
        OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(new LlmProviderRegistry(env), http, objectMapper);

        client.completeJson("system", "user");

        assertThat(http.timeouts.get(0)).isEqualTo(Duration.ofMillis(90_000));
        assertThat(http.bodies.get(0)).contains("\"max_tokens\":8192");
    }

    private String response(String content) {
        return "{\"choices\":[{\"message\":{\"content\":\"" + content + "\"}}]}";
    }

    private static class CapturingHttpClient implements LlmHttpClient {
        final List<String> urls = new ArrayList<>();
        final List<String> bodies = new ArrayList<>();
        final List<Duration> timeouts = new ArrayList<>();
        private final List<LlmHttpResponse> responses = new ArrayList<>();

        CapturingHttpClient(int status, String body) {
            responses.add(new LlmHttpResponse(status, body));
        }

        CapturingHttpClient(int firstStatus, String firstBody, int secondStatus, String secondBody) {
            responses.add(new LlmHttpResponse(firstStatus, firstBody));
            responses.add(new LlmHttpResponse(secondStatus, secondBody));
        }

        @Override
        public LlmHttpResponse postJson(String url, String apiKey, String body, Duration timeout) {
            urls.add(url);
            bodies.add(body);
            timeouts.add(timeout);
            return responses.remove(0);
        }
    }
}
