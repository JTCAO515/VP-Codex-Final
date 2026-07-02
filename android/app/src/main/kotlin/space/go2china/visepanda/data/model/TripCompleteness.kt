package space.go2china.visepanda.data.model

import kotlin.math.roundToInt

/**
 * Native mirror of lib/trips/completeness.ts. Deliberately a pure,
 * side-effect-free function with the exact same six dimensions and the same
 * "vacuously complete when no alert of that type exists" rule for
 * payment/visa, so the readiness percentage a traveler sees never disagrees
 * between the web app and this app for the same TripState.
 */

enum class CompletenessDimension {
    Route,
    Stay,
    Food,
    Transport,
    Payment,
    Visa,
}

data class CompletenessCheck(
    val id: CompletenessDimension,
    val label: String,
    val complete: Boolean,
)

data class CompletenessResult(
    val checks: List<CompletenessCheck>,
    val score: Int,
)

object TripCompleteness {

    fun calculateTripCompleteness(trip: TripState): CompletenessResult {
        fun everyDayHas(predicate: (TripDay) -> Boolean): Boolean =
            trip.days.isNotEmpty() && trip.days.all(predicate)

        fun noOutstandingAlert(type: AlertType): Boolean =
            trip.alerts.filter { it.type == type }.all { it.done }

        val checks = listOf(
            CompletenessCheck(
                CompletenessDimension.Route,
                "Route",
                trip.summary.destinations.isNotEmpty() && trip.days.isNotEmpty(),
            ),
            CompletenessCheck(
                CompletenessDimension.Stay,
                "Stay area",
                everyDayHas { it.stay.isNotBlank() },
            ),
            CompletenessCheck(
                CompletenessDimension.Food,
                "Food",
                everyDayHas { it.food.isNotEmpty() },
            ),
            CompletenessCheck(
                CompletenessDimension.Transport,
                "Transport",
                everyDayHas { it.transport.isNotBlank() },
            ),
            CompletenessCheck(
                CompletenessDimension.Payment,
                "Payment",
                noOutstandingAlert(AlertType.Payment),
            ),
            CompletenessCheck(
                CompletenessDimension.Visa,
                "Visa",
                noOutstandingAlert(AlertType.Visa),
            ),
        )

        val completeCount = checks.count { it.complete }
        // Matches the web app's Math.round((complete / total) * 100) exactly —
        // integer division would truncate instead of round and disagree with
        // the web readiness percentage for non-exact fractions (e.g. 4/6).
        val score = if (checks.isEmpty()) 0 else ((completeCount.toDouble() / checks.size) * 100).roundToInt()
        return CompletenessResult(checks, score)
    }

    fun calculateDayCompleteness(day: TripDay): Int {
        val checks = listOf(
            day.blocks.size >= 3,
            day.food.isNotEmpty(),
            day.stay.isNotBlank(),
            day.transport.isNotBlank(),
        )
        val completeCount = checks.count { it }
        return ((completeCount.toDouble() / checks.size) * 100).roundToInt()
    }
}
