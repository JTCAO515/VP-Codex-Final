package space.go2china.visepanda.butler.agent;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.context.ContextSnapshot;
import space.go2china.visepanda.butler.memory.MemoryContext;
import space.go2china.visepanda.butler.rag.RagSearchResult;
import space.go2china.visepanda.butler.tools.ButlerTools;
import space.go2china.visepanda.butler.tools.ToolBudget;
import space.go2china.visepanda.butler.tools.ToolResult;

@Service
public class LogisticsAgent {
    private final ButlerTools tools;

    public LogisticsAgent(ButlerTools tools) {
        this.tools = tools;
    }

    public AgentResult run(ExecutionStep step, String message, MemoryContext memory, ContextSnapshot context, ToolBudget budget) {
        String normalized = message == null ? "" : message.toLowerCase();
        ToolResult result = normalized.contains("visa") || normalized.contains("passport") || normalized.contains("entry")
                ? tools.getVisaRules(message, budget)
                : normalized.contains("currency") || normalized.contains("exchange")
                ? tools.getExchangeRate(message, budget)
                : new ToolResult("I do not know from verified logistics sources.", List.of("Check the official provider or embassy before relying on this."), "No RAG result");
        List<RagSearchResult> docs = tools.searchKnowledge(message, budget);
        List<String> sources = new ArrayList<>(docs.stream().map(RagSearchResult::sourceLabel).distinct().toList());
        if (!result.sourceLabel().startsWith("No ")) sources.add(result.sourceLabel());
        return new AgentResult("LogisticsAgent", result.summary(), null, result.items(), sources.stream().distinct().toList(), false);
    }
}
