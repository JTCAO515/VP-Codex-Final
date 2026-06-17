package space.jtcao.visepanda.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapRepositoryParseTest {

    @Test
    fun `map response object should parse into marker list`() {
        val json = """
            {
              "cities": {
                "beijing": {"lat": 39.9042, "lng": 116.4074},
                "shanghai": {"lat": 31.2304, "lng": 121.4737}
              }
            }
        """.trimIndent()

        val markers = MapRepository.parseMarkersForTest(json)

        assertEquals(2, markers.size)
        assertEquals("beijing", markers[0].name)
        assertTrue(markers[0].lat > 0)
    }
}
