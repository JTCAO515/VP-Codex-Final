package space.go2china.visepanda.butler.model;

import java.util.List;

public record TripBlock(
        String time,
        String title,
        String description,
        List<String> highlights,
        String photoUrl,
        String address,
        String chineseAddress,
        String phone,
        String openingHours,
        String mapUrl,
        String bookingUrl,
        List<BookingCandidate> bookingCandidates,
        String sourceLabel,
        Coordinates coordinates
) {
}
