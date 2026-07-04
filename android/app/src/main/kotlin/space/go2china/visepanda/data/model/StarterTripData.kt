package space.go2china.visepanda.data.model

object StarterTripData {
    val initialTripState: TripState = TripState(
        summary = TripSummary(
            title = "New China Trip",
            durationDays = 0,
            pace = Pace.Balanced,
            travelerStyle = "Not set yet",
            destinations = emptyList(),
            confidence = TripConfidence.Draft,
        ),
        days = emptyList(),
        alerts = emptyList(),
        lastUpdatedReason = "Starter draft",
    )
}
