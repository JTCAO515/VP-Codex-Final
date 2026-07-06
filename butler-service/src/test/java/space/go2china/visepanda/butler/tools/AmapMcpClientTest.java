package space.go2china.visepanda.butler.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

class AmapMcpClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void callsOfficialStreamableHttpTool() throws Exception {
        Environment env = mock(Environment.class);
        when(env.getProperty("AMAP_API_KEY")).thenReturn("test-key");
        AmapMcpTransport transport = (url, body, timeout) -> {
            assertThat(url).isEqualTo("https://mcp.amap.com/mcp?key=test-key");
            assertThat(timeout).isEqualTo(Duration.ofSeconds(8));
            assertThat(body).contains("\"method\":\"tools/call\"");
            assertThat(body).contains("\"name\":\"maps_text_search\"");
            assertThat(body).contains("\"keywords\":\"recommend lunch in Shanghai\"");
            assertThat(body).contains("\"city\":\"上海\"");
            return """
                    {"jsonrpc":"2.0","id":"vp-poi-search","result":{"content":[{"type":"text","text":"上海本帮菜馆 rating 4.6"}]}}
                    """;
        };

        ToolResult result = new AmapMcpClient(env, transport, objectMapper)
                .textSearch("recommend lunch in Shanghai")
                .orElseThrow();

        assertThat(result.summary()).isEqualTo("Realtime POI candidates");
        assertThat(result.items()).containsExactly("上海本帮菜馆 rating 4.6");
        assertThat(result.sourceLabel()).isEqualTo("Amap MCP");
    }

    @Test
    void failureReturnsHonestEmptyResult() {
        Environment env = mock(Environment.class);
        when(env.getProperty("AMAP_MCP_URL")).thenReturn("https://mcp.amap.com/mcp?key=test-key");
        AmapMcpTransport transport = (url, body, timeout) -> {
            throw new IllegalStateException("boom");
        };

        ToolResult result = new AmapMcpClient(env, transport, objectMapper)
                .textSearch("nearby hotpot")
                .orElseThrow();

        assertThat(result.summary()).isEqualTo("Realtime POI lookup is unavailable.");
        assertThat(result.items()).isEmpty();
        assertThat(result.sourceLabel()).isEqualTo("Amap MCP");
    }

    @Test
    void missingKeyLeavesToolUnconfigured() {
        Environment env = mock(Environment.class);
        assertThat(new AmapMcpClient(env, (url, body, timeout) -> "{}", objectMapper).textSearch("poi")).isEmpty();
    }
}
