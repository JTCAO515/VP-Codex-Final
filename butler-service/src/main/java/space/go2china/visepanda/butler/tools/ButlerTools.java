package space.go2china.visepanda.butler.tools;

import dev.langchain4j.agent.tool.Tool;
import java.util.List;
import org.springframework.stereotype.Component;
import space.go2china.visepanda.butler.rag.RagSearchResult;
import space.go2china.visepanda.butler.rag.RagService;

@Component
public class ButlerTools {
    private final RagService ragService;
    private final AmapMcpClient amapMcpClient;

    public ButlerTools(RagService ragService, AmapMcpClient amapMcpClient) {
        this.ragService = ragService;
        this.amapMcpClient = amapMcpClient;
    }

    @Tool("Search nearby POIs from Amap MCP when configured")
    public ToolResult searchPois(String query, ToolBudget budget) {
        if (!budget.tryUse()) return new ToolResult("Tool budget exhausted.", List.of(), "Tool budget");
        return amapMcpClient.textSearch(query)
                .orElseGet(() -> new ToolResult("Realtime POI lookup is not configured.", List.of(), "Amap MCP"));
    }

    @Tool("Get conservative exchange-rate guidance")
    public ToolResult getExchangeRate(String query, ToolBudget budget) {
        if (!budget.tryUse()) return new ToolResult("Tool budget exhausted.", List.of(), "Tool budget");
        return new ToolResult("Live exchange rate is not configured.", List.of("Use your bank or trusted currency app before large exchanges.", "Keep a rough RMB conversion offline."), "Static Tools knowledge");
    }

    @Tool("Get conservative visa-rule guidance")
    public ToolResult getVisaRules(String query, ToolBudget budget) {
        if (!budget.tryUse()) return new ToolResult("Tool budget exhausted.", List.of(), "Tool budget");
        List<RagSearchResult> docs = ragService.search(query + " visa entry passport transit", 2);
        if (docs.isEmpty()) return new ToolResult("I do not know the current rule from available sources.", List.of("Confirm with the nearest Chinese embassy or consulate."), "No RAG result");
        return new ToolResult(docs.get(0).snippet(), docs.stream().map(RagSearchResult::sourceLabel).distinct().toList(), docs.get(0).sourceLabel());
    }

    @Tool("Translate a traveler phrase with deterministic fallback")
    public ToolResult translate(String phrase, ToolBudget budget) {
        if (!budget.tryUse()) return new ToolResult("Tool budget exhausted.", List.of(), "Tool budget");
        return new ToolResult("Translation service is not configured.", List.of("Show this request in your translation app: " + phrase), "Mock translation fallback");
    }

    public List<RagSearchResult> searchKnowledge(String query, ToolBudget budget) {
        if (!budget.tryUse()) return List.of();
        return ragService.search(query, 3);
    }
}
