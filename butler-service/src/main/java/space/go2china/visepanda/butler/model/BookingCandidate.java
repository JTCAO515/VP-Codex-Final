package space.go2china.visepanda.butler.model;

public record BookingCandidate(
        String id,
        String kind,
        String label,
        String provider,
        String status,
        String note,
        String url,
        String priceHint
) {
}
