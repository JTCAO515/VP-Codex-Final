package space.go2china.visepanda.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasPatchApplierTest {

    @Test
    fun mergesSummaryFieldsAndReplacesSuppliedDays() {
        val initial = MockTripData.initialTripState
        val replacementDay = initial.days.first().copy(
            city = "Nanjing",
            pace = Pace.Relaxed,
        )
        val patch = CanvasPatch(
            intent = CanvasPatchIntent.AdjustTrip,
            assistantMessage = "Updated.",
            tripSummary = TripSummaryPatch(
                title = "Nanjing 3-Day Trip",
                pace = Pace.Relaxed,
                destinations = listOf("Nanjing"),
            ),
            days = listOf(replacementDay),
            reason = "Changed destination.",
        )

        val next = CanvasPatchApplier.apply(initial, patch)

        assertEquals("Nanjing 3-Day Trip", next.summary.title)
        assertEquals(Pace.Relaxed, next.summary.pace)
        assertEquals(listOf("Nanjing"), next.summary.destinations)
        assertEquals(1, next.days.size)
        assertEquals("Nanjing", next.days.first().city)
        assertEquals("Changed destination.", next.lastUpdatedReason)
    }

    @Test
    fun deduplicatesAlertsByTypeAndTitle() {
        val initial = MockTripData.initialTripState
        val alert = initial.alerts.first()
        val patch = CanvasPatch(
            intent = CanvasPatchIntent.AddAlerts,
            assistantMessage = "Added.",
            butlerAlerts = listOf(alert.copy(body = "Updated body.")),
            reason = "Alert update.",
        )

        val next = CanvasPatchApplier.apply(initial.copy(alerts = listOf(alert)), patch)

        assertEquals(1, next.alerts.size)
        assertEquals("Updated body.", next.alerts.first().body)
        assertTrue(next.days.isNotEmpty())
    }
}
