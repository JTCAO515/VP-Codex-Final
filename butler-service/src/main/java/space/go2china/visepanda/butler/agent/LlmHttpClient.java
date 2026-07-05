package space.go2china.visepanda.butler.agent;

import java.time.Duration;

public interface LlmHttpClient {
    LlmHttpResponse postJson(String url, String apiKey, String body, Duration timeout) throws Exception;
}
