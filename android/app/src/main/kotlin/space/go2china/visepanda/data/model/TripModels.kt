package space.go2china.visepanda.data.model

/**
 * Native mirror of lib/types/trip.ts. Field names and shapes are kept 1:1
 * with the web contract on purpose: the v0.3.5 Butler+Sync round will
 * deserialize the same /api/chat and /api/trips JSON into these classes, so
 * drift here becomes a real bug later, not just an inconsistency.
 */

enum class Pace {
    Light,
    Balanced,
    Relaxed,
    Packed,
}

enum class AlertPriority {
    High,
    Medium,
    Low,
}

enum class AlertType {
    Visa,
    Payment,
    Booking,
    Transport,
    Weather,
    Language,
    Risk,
    Emergency,
}

enum class BlockTime {
    Morning,
    Afternoon,
    Evening,
    Flexible,
}

enum class BookingCandidateKind {
    Hotel,
    Ticket,
    Transport,
    Restaurant,
}

enum class BookingCandidateStatus {
    /** Never rendered as purchasable — see AGENTS.md booking-trust rules. */
    InfoOnly,
    Planned,
}

data class Coordinates(
    val lat: Double,
    val lng: Double,
)

data class BookingCandidate(
    val id: String,
    val kind: BookingCandidateKind,
    val label: String,
    val provider: String,
    val status: BookingCandidateStatus,
    val note: String,
    val url: String? = null,
    val priceHint: String? = null,
)

data class TripBlock(
    val time: BlockTime,
    val title: String,
    val description: String,
    val highlights: List<String> = emptyList(),
    val photoUrl: String? = null,
    val address: String? = null,
    val chineseAddress: String? = null,
    val phone: String? = null,
    val openingHours: String? = null,
    val mapUrl: String? = null,
    val bookingUrl: String? = null,
    val bookingCandidates: List<BookingCandidate> = emptyList(),
    val sourceLabel: String? = null,
    val coordinates: Coordinates? = null,
)

enum class DayStatus {
    New,
    Revised,
    NeedsConfirmation,
}

data class TripDay(
    val day: Int,
    val city: String,
    val pace: Pace,
    val blocks: List<TripBlock>,
    val food: List<String>,
    val stay: String,
    val transport: String,
    val note: String,
    val status: DayStatus? = null,
)

data class ButlerAlert(
    val type: AlertType,
    val priority: AlertPriority,
    val title: String,
    val body: String,
    val action: String,
    /**
     * Operational checklist state (e.g. "Before you fly"), not itinerary
     * content — toggling this is a local mutation and does not need to go
     * through the Butler pipeline. Mirrors the same rule as the web app's
     * ButlerAlert.done (see AGENTS.md v0.2.7 note).
     */
    val done: Boolean = false,
)

enum class TripConfidence {
    Draft,
    Refined,
    ReadyToSave,
}

data class TripSummary(
    val title: String,
    val durationDays: Int,
    val pace: Pace,
    val travelerStyle: String,
    val destinations: List<String>,
    val confidence: TripConfidence,
)

data class TripState(
    val summary: TripSummary,
    val days: List<TripDay>,
    val alerts: List<ButlerAlert>,
    val lastUpdatedReason: String,
)

/**
 * Non-persisted, UI-only classification of "where in the trip is the
 * traveler right now" — computed on the client from [TripState] plus a
 * clock, never stored. Drives the Today screen's Now/Next/Later timeline.
 */
enum class TimelinePosition {
    Now,
    Next,
    Later,
}

data class TimelineEntry(
    val day: TripDay,
    val block: TripBlock,
    val position: TimelinePosition,
)
