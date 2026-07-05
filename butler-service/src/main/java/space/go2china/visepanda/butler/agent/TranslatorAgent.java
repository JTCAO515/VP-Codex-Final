package space.go2china.visepanda.butler.agent;

import org.springframework.stereotype.Service;
import space.go2china.visepanda.butler.context.ContextSnapshot;
import space.go2china.visepanda.butler.memory.MemoryContext;
import space.go2china.visepanda.butler.tools.ButlerTools;
import space.go2china.visepanda.butler.tools.ToolBudget;
import space.go2china.visepanda.butler.tools.ToolResult;

@Service
public class TranslatorAgent {
    private final ButlerTools tools;

    public TranslatorAgent(ButlerTools tools) {
        this.tools = tools;
    }

    public AgentResult run(ExecutionStep step, String message, MemoryContext memory, ContextSnapshot context, ToolBudget budget) {
        ToolResult result = tools.translate(message, budget);
        return new AgentResult("TranslatorAgent", result.summary(), null, result.items(), java.util.List.of(result.sourceLabel()), false);
    }
}
