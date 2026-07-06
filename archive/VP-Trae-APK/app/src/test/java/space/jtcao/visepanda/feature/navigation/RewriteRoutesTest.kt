package space.jtcao.visepanda.feature.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RewriteRoutesTest {

    @Test
    fun `city detail route should encode city id`() {
        assertEquals("destination/shanghai", RewriteRoutes.destination("shanghai"))
    }

    @Test
    fun `chat route should encode city id`() {
        assertEquals("chat/shanghai", RewriteRoutes.chat("shanghai"))
    }
}
