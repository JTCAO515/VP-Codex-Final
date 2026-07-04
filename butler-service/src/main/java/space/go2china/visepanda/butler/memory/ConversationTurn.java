package space.go2china.visepanda.butler.memory;

import java.time.Instant;

public record ConversationTurn(String role, String content, Instant createdAt) {
}
