package space.go2china.visepanda.data.model

/**
 * Computes the Today screen's Now/Next/Later timeline.
 *
 * Known, disclosed limitation: [TripState] has no trip start date field yet
 * (see lib/types/trip.ts — the web contract does not have one either), so
 * there is no real way to know which [TripDay] is "today" in wall-clock
 * time. v0.3.4 uses the first day in the list as a stand-in for "today" and
 * the first/second blocks within it as Now/Next — this is an honest mock
 * simplification for demoing the Today screen shape, not a claim that the
 * app knows the traveler's real current time position. A real
 * date-anchored TripState field is a v0.3.5+ concern once the Butler/sync
 * bridge exists to actually populate one.
 */
object TripTimeline {

    fun buildTimeline(trip: TripState): List<TimelineEntry> {
        val today = trip.days.firstOrNull() ?: return emptyList()
        return today.blocks.mapIndexed { index, block ->
            val position = when (index) {
                0 -> TimelinePosition.Now
                1 -> TimelinePosition.Next
                else -> TimelinePosition.Later
            }
            TimelineEntry(day = today, block = block, position = position)
        }
    }
}
