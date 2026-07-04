package space.go2china.visepanda.butler.memory;

import java.time.Instant;

public record TripMemoryEntry(String tripKey, String kind, String body, Instant updatedAt) {
}
