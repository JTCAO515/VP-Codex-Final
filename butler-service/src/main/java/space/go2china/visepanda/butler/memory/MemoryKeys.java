package space.go2china.visepanda.butler.memory;

import space.go2china.visepanda.butler.model.ChatRequest;

public final class MemoryKeys {
    private MemoryKeys() {
    }

    public static String userKey(ChatRequest request) {
        if (hasText(request.userId())) return "user:" + request.userId().trim();
        if (hasText(request.guestId())) return "guest:" + request.guestId().trim();
        if (hasText(request.sessionId())) return "guest-session:" + request.sessionId().trim();
        return "guest:anonymous";
    }

    public static String sessionKey(ChatRequest request) {
        if (hasText(request.sessionId())) return request.sessionId().trim();
        return userKey(request);
    }

    public static String tripKey(ChatRequest request) {
        if (hasText(request.tripId())) return request.tripId().trim();
        return sessionKey(request);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
