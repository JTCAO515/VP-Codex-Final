package space.go2china.visepanda.butler.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class JavaNetLlmHttpClient implements LlmHttpClient {
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public LlmHttpResponse postJson(String url, String apiKey, String body, Duration timeout) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new LlmHttpResponse(response.statusCode(), response.body());
    }
}
