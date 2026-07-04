package space.go2china.visepanda.butler.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import space.go2china.visepanda.butler.memory.MemoryContext;

@Component
public class SupabaseChatLogSink implements ChatLogSink {
    private final SupabaseProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final int retentionDays;

    public SupabaseChatLogSink(SupabaseProperties properties, ObjectMapper objectMapper,
                               @Value("${butler.memory.retention-days:30}") int retentionDays) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.retentionDays = retentionDays;
    }

    @Override
    public void save(MemoryContext context, String userMessage, String assistantMessage) {
        if (!properties.configured()) return;
        try {
            Map<String, Object> body = Map.of(
                    "session_key", context.sessionKey(),
                    "user_key", context.userKey(),
                    "trip_key", context.tripKey(),
                    "user_message", userMessage,
                    "assistant_message", assistantMessage,
                    "expires_at", Instant.now().plus(retentionDays, ChronoUnit.DAYS).toString()
            );
            post("chat_logs", objectMapper.writeValueAsString(body));
        } catch (Exception ignored) {
            // ponytail: persistence is best-effort; chat must keep working without Supabase.
        }
    }

    private void post(String table, String json) throws Exception {
        String base = properties.url().replaceAll("/+$", "");
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/rest/v1/" + table))
                .header("apikey", properties.serviceRoleKey())
                .header("Authorization", "Bearer " + properties.serviceRoleKey())
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }
}
