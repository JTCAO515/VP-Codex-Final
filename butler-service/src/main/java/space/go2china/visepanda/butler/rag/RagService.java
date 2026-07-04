package space.go2china.visepanda.butler.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.persistence.SupabaseProperties;

@Service
public class RagService {
    private final SupabaseProperties supabase;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final List<RagDocument> STATIC_DOCS = List.of(
            new RagDocument("visa-entry", "China visa and entry basics",
                    "Visa-free and transit eligibility depend on nationality, city pair, route, and trip length. Confirm current rules with the nearest Chinese embassy or consulate before booking.",
                    "Static Tools knowledge"),
            new RagDocument("payment", "Alipay and WeChat Pay setup",
                    "Install Alipay or WeChat Pay before arrival, link an international card, and keep RMB cash as a backup for taxis, small vendors, or app verification failures.",
                    "Static Tools knowledge"),
            new RagDocument("metro", "China metro basics",
                    "Large Chinese cities have metro ticket machines and app QR codes. Save destination station names in Chinese and check last-train times before late dinners.",
                    "Static Tools knowledge"),
            new RagDocument("emergency", "Emergency contacts in China",
                    "For urgent danger call local emergency numbers and contact your embassy, hotel, and travel insurer. Keep passport and insurance details available offline.",
                    "Static Tools knowledge"),
            new RagDocument("translation", "Taxi and dining translation prep",
                    "Save hotel addresses and common food phrases in Chinese before you land. Show Chinese text to drivers and restaurant staff when needed.",
                    "Static Tools knowledge")
    );

    public RagService(SupabaseProperties supabase, ObjectMapper objectMapper) {
        this.supabase = supabase;
        this.objectMapper = objectMapper;
    }

    public List<RagSearchResult> search(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();
        List<RagSearchResult> live = searchSupabase(query, limit);
        if (!live.isEmpty()) return live;
        return searchStatic(query, limit);
    }

    private List<RagSearchResult> searchSupabase(String query, int limit) {
        if (!supabase.configured()) return List.of();
        try {
            String base = supabase.url().replaceAll("/+$", "");
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/rest/v1/rag_documents?select=title,body,source_label&body=ilike.*" + encoded + "*&limit=" + limit))
                    .header("apikey", supabase.serviceRoleKey())
                    .header("Authorization", "Bearer " + supabase.serviceRoleKey())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) return List.of();
            JsonNode rows = objectMapper.readTree(response.body());
            java.util.ArrayList<RagSearchResult> results = new java.util.ArrayList<>();
            for (JsonNode row : rows) {
                results.add(new RagSearchResult(row.path("title").asText(), snippet(row.path("body").asText()), row.path("source_label").asText("Supabase RAG"), 1.0));
            }
            return results;
        } catch (Exception ignored) {
            // ponytail: pgvector/PostgREST is optional; static keyword search is the fallback.
            return List.of();
        }
    }

    private List<RagSearchResult> searchStatic(String query, int limit) {
        String normalized = query.toLowerCase(Locale.ROOT);
        return STATIC_DOCS.stream()
                .map(doc -> new RagSearchResult(doc.title(), snippet(doc.body()), doc.sourceLabel(), score(normalized, doc)))
                .filter(result -> result.score() > 0)
                .sorted(Comparator.comparingDouble(RagSearchResult::score).reversed())
                .limit(limit)
                .toList();
    }

    private double score(String query, RagDocument doc) {
        double score = 0;
        String haystack = (doc.title() + " " + doc.body()).toLowerCase(Locale.ROOT);
        for (String token : query.split("\\W+")) {
            if (token.length() > 2 && haystack.contains(token)) score += 1;
        }
        return score;
    }

    private String snippet(String body) {
        if (body == null) return "";
        return body.length() <= 180 ? body : body.substring(0, 180);
    }
}
