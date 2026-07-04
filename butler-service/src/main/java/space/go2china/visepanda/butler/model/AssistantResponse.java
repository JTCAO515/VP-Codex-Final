package space.go2china.visepanda.butler.model;

import java.util.List;

public record AssistantResponse(
        String headline,
        String body,
        List<String> highlights,
        String watchOut,
        String nextStep,
        List<InlineToolCard> toolCards,
        List<String> followUp,
        String toneHint
) {
    public AssistantResponse {
        highlights = highlights == null ? List.of() : List.copyOf(highlights);
        followUp = followUp == null ? null : List.copyOf(followUp);
    }
}
