package space.jtcao.visepanda.data.trip

import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.data.model.Trip

class TripAssetRepositoryImplTest {

    @Test
    fun `legacy trip should map into trip asset`() {
        val legacy = Trip(
            id = "1",
            title = "Shanghai 3-day trip",
            city = "shanghai",
            days = 3,
            content = "Day 1..."
        )

        val asset = legacy.toTripAsset()

        assertEquals("1", asset.id)
        assertEquals("shanghai", asset.cityId)
    }
}
