package space.go2china.visepanda.butler.model;

import java.util.List;

public record TripState(
        TripSummary summary,
        List<TripDay> days,
        List<ButlerAlert> alerts,
        String lastUpdatedReason
) {
}
