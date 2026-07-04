package space.go2china.visepanda.butler.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.springframework.stereotype.Component;
import space.go2china.visepanda.butler.memory.TripMemoryEntry;
import space.go2china.visepanda.butler.memory.UserMemoryEntry;

@Component
public class SupabaseMemoryPersistenceSink implements MemoryPersistenceSink {
    private final SupabaseProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SupabaseMemoryPersistenceSink(SupabaseProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveUserMemory(String userKey, UserMemoryEntry entry) {
        if (!properties.configured()) return;
        try {
            post("user_memories", objectMapper.writeValueAsString(Map.of(
                    "user_key", userKey,
                    "memory_key", entry.key(),
                    "memory_value", entry.value(),
                    "confidence", entry.confidence(),
                    "evidence", entry.evidence(),
                    "source", entry.source(),
                    "updated_at", entry.updatedAt().toString()
            )));
        } catch (Exception ignored) {
            // ponytail: Supabase is write-through only; in-memory fallback remains authoritative for this process.
        }
    }

    @Override
    public void saveTripMemory(TripMemoryEntry entry) {
        if (!properties.configured()) return;
        try {
            post("trip_memories", objectMapper.writeValueAsString(Map.of(
                    "trip_key", entry.tripKey(),
                    "kind", entry.kind(),
                    "body", entry.body(),
                    "updated_at", entry.updatedAt().toString()
            )));
        } catch (Exception ignored) {
            // ponytail: Supabase is write-through only; chat must not fail if persistence is down.
        }
    }

    private void post(String table, String json) throws Exception {
        String base = properties.url().replaceAll("/+$", "");
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/rest/v1/" + table))
                .header("apikey", properties.serviceRoleKey())
                .header("Authorization", "Bearer " + properties.serviceRoleKey())
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates,return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }
}
