package space.go2china.visepanda.butler.model;

import java.util.List;

public record InlineToolCard(
        String id,
        String categoryId,
        String title,
        String summary,
        List<String> items,
        String nextAction,
        String href,
        String tone,
        String sourceLabel
) {
}
