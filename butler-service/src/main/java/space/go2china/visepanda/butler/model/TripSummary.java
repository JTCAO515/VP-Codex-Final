package space.go2china.visepanda.butler.model;

import java.util.List;

public record TripSummary(
        String title,
        Integer durationDays,
        String pace,
        String travelerStyle,
        List<String> destinations,
        String confidence
) {
}
