package space.go2china.visepanda.butler.agent;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.context.ContextSnapshot;
import space.go2china.visepanda.butler.memory.MemoryContext;
import space.go2china.visepanda.butler.model.CanvasPatch;
import space.go2china.visepanda.butler.rag.RagSearchResult;
import space.go2china.visepanda.butler.tools.ButlerTools;
import space.go2china.visepanda.butler.tools.ToolBudget;

@Service
public class LocalExpertAgent {
    private final ButlerTools tools;

    public LocalExpertAgent(ButlerTools tools) {
        this.tools = tools;
    }

    public AgentResult run(ExecutionStep step, String message, MemoryContext memory, ContextSnapshot context, ToolBudget budget) {
        List<String> highlights = new ArrayList<>(tools.searchPois(message, budget).items());
        List<RagSearchResult> docs = tools.searchKnowledge(message + " food culture POI", budget);
        List<String> sources = docs.stream().map(RagSearchResult::sourceLabel).distinct().toList();
        String summary = highlights.isEmpty()
                ? "I could not verify a local recommendation from the available sources. Check a current map or venue listing before going."
                : "Local options: " + String.join("; ", highlights);
        if (!docs.isEmpty()) summary += " Source: " + docs.get(0).sourceLabel() + ".";
        return new AgentResult("LocalExpertAgent", summary, null, highlights, sources, false);
    }
}
