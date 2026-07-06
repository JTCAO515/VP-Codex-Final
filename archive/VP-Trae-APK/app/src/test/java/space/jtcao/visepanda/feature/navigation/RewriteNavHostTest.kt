package space.jtcao.visepanda.feature.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RewriteNavHostTest {

    @Test
    fun `home featured destination should navigate to destination route`() {
        assertEquals(
            "destination/shanghai",
            featuredDestinationRoute("shanghai")
        )
    }
}
