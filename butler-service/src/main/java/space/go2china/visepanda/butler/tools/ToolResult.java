package space.go2china.visepanda.butler.tools;

import java.util.List;

public record ToolResult(String summary, List<String> items, String sourceLabel) {
    public ToolResult {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
