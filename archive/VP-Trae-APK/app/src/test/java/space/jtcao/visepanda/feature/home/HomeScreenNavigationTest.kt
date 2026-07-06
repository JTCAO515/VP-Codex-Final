package space.jtcao.visepanda.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.domain.model.DestinationSummary

class HomeScreenNavigationTest {

    @Test
    fun `featured destination click should open destination for selected city`() {
        var openedCityId: String? = null
        val onFeaturedDestinationClick = featuredDestinationClickHandler { cityId ->
            openedCityId = cityId
        }

        onFeaturedDestinationClick(
            DestinationSummary(
                id = "shanghai",
                name = "Shanghai",
                tagline = "Future Skyline",
                vibe = "Art deco nights",
                lat = 31.2304,
                lng = 121.4737
            )
        )

        assertEquals("shanghai", openedCityId)
    }
}
