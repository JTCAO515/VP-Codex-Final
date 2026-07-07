package space.go2china.visepanda.butler.rag;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import space.go2china.visepanda.butler.persistence.SupabaseProperties;

class RagServiceTest {
    private final RagService rag = new RagService(new SupabaseProperties("", ""), new ObjectMapper());

    @Test
    void returnsStaticKnowledgeWhenKeywordMatches() {
        assertThat(rag.search("Alipay payment backup cash", 2)).isNotEmpty();
    }

    @Test
    void returnsEmptyWhenNothingMatchesSoAgentsCannotInvent() {
        assertThat(rag.search("obscure lunar yak ferry permit", 2)).isEmpty();
    }
}
