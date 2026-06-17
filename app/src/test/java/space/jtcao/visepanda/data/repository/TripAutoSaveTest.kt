package space.jtcao.visepanda.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TripAutoSaveTest {

    @Test
    fun `itinerary style content should produce trip`() {
        val content = """
            ### Trip Overview
            **Day 1: Forbidden City**
            - Morning: Visit Tiananmen
            **Day 2: Great Wall**
        """.trimIndent()

        val trip = TripAutoSave.createTripOrNull(city = "beijing", content = content)

        assertNotNull(trip)
        assertEquals("beijing", trip?.city)
        assertEquals(2, trip?.days)
    }

    @Test
    fun `generic answer should not produce trip`() {
        val trip = TripAutoSave.createTripOrNull(
            city = "beijing",
            content = "China has many high speed trains and mobile payments."
        )

        assertNull(trip)
    }
}
