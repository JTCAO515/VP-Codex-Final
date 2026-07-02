package space.go2china.visepanda.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import space.go2china.visepanda.data.model.CanvasPatchApplier
import space.go2china.visepanda.data.model.MockTripData
import space.go2china.visepanda.data.model.TripConfidence

class NativeButlerFallbackTest {

    @Test
    fun createsSafeOfflinePatchForNanjingRequest() {
        val initial = MockTripData.initialTripState
        val patch = NativeButlerFallback.createPatch("Please make this a Nanjing trip", initial)

        val next = CanvasPatchApplier.apply(initial, patch)

        assertEquals("Nanjing 3-Day Trip", next.summary.title)
        assertEquals(TripConfidence.Refined, next.summary.confidence)
        assertTrue(patch.assistantMessage.contains("saved", ignoreCase = true))
        assertNotNull(patch.assistantResponse)
    }

    @Test
    fun doesNotInventPurchasableBookingState() {
        val patch = NativeButlerFallback.createPatch("Book me a hotel", MockTripData.initialTripState)

        assertEquals(null, patch.days)
        assertEquals(null, patch.butlerAlerts)
    }
}
