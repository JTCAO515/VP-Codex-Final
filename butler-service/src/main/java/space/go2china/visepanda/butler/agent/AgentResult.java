package space.go2china.visepanda.butler.agent;

import java.util.List;
import space.go2china.visepanda.butler.model.CanvasPatch;

public record AgentResult(
        String agent,
        String summary,
        CanvasPatch patch,
        List<String> highlights,
        List<String> sources,
        boolean partial
) {
    public AgentResult {
        highlights = highlights == null ? List.of() : List.copyOf(highlights);
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
