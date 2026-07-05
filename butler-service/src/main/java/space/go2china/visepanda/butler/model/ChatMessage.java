package space.go2china.visepanda.butler.model;

import java.util.List;

public record ChatMessage(
        String id,
        String role,
        String content,
        AssistantResponse response,
        List<ChangeDigestEntry> changeDigest,
        String createdAt
) {
}
