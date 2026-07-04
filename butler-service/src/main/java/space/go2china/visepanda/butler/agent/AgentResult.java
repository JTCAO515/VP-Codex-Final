package space.go2china.visepanda.butler.agent;

import java.util.List;
import space.go2china.visepanda.butler.model.CanvasPatch;

public record AgentResult(
        String agent,
        String summary,
        CanvasPatch patch,
        List<String> highlights,
        List<String> sources,
        boolean partial,
        String mode,
        String modelLabel,
        List<String> providersTried
) {
    public AgentResult(String agent, String summary, CanvasPatch patch, List<String> highlights, List<String> sources,
                       boolean partial) {
        this(agent, summary, patch, highlights, sources, partial, null, null, List.of());
    }

    public AgentResult {
        highlights = highlights == null ? List.of() : List.copyOf(highlights);
        sources = sources == null ? List.of() : List.copyOf(sources);
        providersTried = providersTried == null ? List.of() : List.copyOf(providersTried);
    }
}
