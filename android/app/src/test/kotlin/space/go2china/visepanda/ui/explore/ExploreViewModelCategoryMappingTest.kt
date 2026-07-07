package space.go2china.visepanda.ui.explore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import space.go2china.visepanda.data.explore.ExploreCategory

/**
 * Regression coverage for the Chat↔Explore bridge (Issue #59): a Butler
 * exploreRef card carries ExploreRefCategory.name ("Attractions"/"Food"/
 * "Stays") and ExploreViewModel maps it back to the richer ExploreCategory
 * used by the channel screens. A silent mismatch here would send the
 * traveler to the wrong channel (or nowhere) after tapping a real POI card.
 */
class ExploreViewModelCategoryMappingTest {

    @Test
    fun `maps every real ExploreRefCategory name to the matching ExploreCategory`() {
        assertEquals(ExploreCategory.Attraction, exploreCategoryFromRefCategory("Attractions"))
        assertEquals(ExploreCategory.Food, exploreCategoryFromRefCategory("Food"))
        assertEquals(ExploreCategory.Stay, exploreCategoryFromRefCategory("Stays"))
    }

    @Test
    fun `returns null for unknown or malformed category strings instead of guessing`() {
        assertNull(exploreCategoryFromRefCategory("shopping"))
        assertNull(exploreCategoryFromRefCategory(""))
        assertNull(exploreCategoryFromRefCategory("attractions"))
    }
}
