package space.go2china.visepanda.butler.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record ChatRequest(
        String message,
        TripState trip,
        List<ChatMessage> messages,
        JsonNode preferenceProfile,
        String sessionId,
        String guestId,
        String userId,
        String tripId,
        String currentCity,
        Integer currentDay,
        String entryPoint,
        String departureDate
) {
}
