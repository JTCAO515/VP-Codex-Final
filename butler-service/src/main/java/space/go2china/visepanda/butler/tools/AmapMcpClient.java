package space.go2china.visepanda.butler.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AmapMcpClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(8);
    private final Environment env;
    private final AmapMcpTransport transport;
    private final ObjectMapper objectMapper;

    public AmapMcpClient(Environment env, AmapMcpTransport transport, ObjectMapper objectMapper) {
        this.env = env;
        this.transport = transport;
        this.objectMapper = objectMapper;
    }

    public Optional<ToolResult> textSearch(String query) {
        String url = endpoint().orElse(null);
        if (url == null) return Optional.empty();
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "jsonrpc", "2.0",
                    "id", "vp-poi-search",
                    "method", "tools/call",
                    "params", Map.of(
                            "name", "maps_text_search",
                            "arguments", Map.of("keywords", query == null ? "" : query, "city", city(query)))));
            String response = transport.post(url, body, TIMEOUT);
            List<String> items = extractItems(response);
            if (items.isEmpty()) {
                return Optional.of(new ToolResult("Realtime POI lookup returned no verified places.", List.of(), "Amap MCP"));
            }
            return Optional.of(new ToolResult("Realtime POI candidates", items, "Amap MCP"));
        } catch (Exception ignored) {
            return Optional.of(new ToolResult("Realtime POI lookup is unavailable.", List.of(), "Amap MCP"));
        }
    }

    private Optional<String> endpoint() {
        String explicit = trim(env.getProperty("AMAP_MCP_URL"));
        if (explicit != null) return Optional.of(explicit);
        String key = trim(env.getProperty("AMAP_API_KEY"));
        if (key == null) key = trim(env.getProperty("AMAP_MAPS_API_KEY"));
        return key == null ? Optional.empty() : Optional.of("https://mcp.amap.com/mcp?key=" + key);
    }

    private String city(String query) {
        String normalized = query == null ? "" : query.toLowerCase();
        if (normalized.contains("shanghai")) return "上海";
        if (normalized.contains("chengdu")) return "成都";
        if (normalized.contains("xian") || normalized.contains("xi'an")) return "西安";
        return "北京";
    }

    private List<String> extractItems(String raw) throws Exception {
        JsonNode root = objectMapper.readTree(jsonPayload(raw));
        JsonNode content = root.path("result").path("content");
        List<String> items = new ArrayList<>();
        if (content.isArray()) {
            for (JsonNode item : content) {
                String text = item.path("text").asText("");
                if (!text.isBlank()) items.add(text);
            }
        }
        return items.stream().limit(5).toList();
    }

    private String jsonPayload(String raw) {
        if (raw == null) return "{}";
        for (String line : raw.split("\\R")) {
            if (line.startsWith("data:")) return line.substring(5).trim();
        }
        return raw;
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

interface AmapMcpTransport {
    String post(String url, String body, Duration timeout) throws Exception;
}

@Component
class JavaNetAmapMcpTransport implements AmapMcpTransport {
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public String post(String url, String body, Duration timeout) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IllegalStateException("Amap MCP HTTP " + response.statusCode());
        return response.body();
    }
}
