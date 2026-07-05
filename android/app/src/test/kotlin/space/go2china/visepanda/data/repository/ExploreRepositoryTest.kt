package space.go2china.visepanda.data.repository

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import space.go2china.visepanda.data.explore.ExploreCategory
import space.go2china.visepanda.data.model.BookingCandidateKind
import space.go2china.visepanda.data.model.BookingCandidateStatus
import space.go2china.visepanda.data.remote.AmapPoiJson
import space.go2china.visepanda.data.remote.ExploreAmapResponse
import space.go2china.visepanda.data.remote.ExploreApiService
import java.io.IOException

class ExploreRepositoryTest {

    private class MockExploreApiService : ExploreApiService {
        var shouldFail = false
        var mockResponse: ExploreAmapResponse? = null

        override suspend fun searchAmapPois(
            cityId: String,
            type: String,
            keyword: String?,
            mode: String?,
            location: String?,
            radius: Int?,
            sort: String?,
            page: Int?,
        ): ExploreAmapResponse {
            if (shouldFail) throw IOException("Network disconnect simulated")
            return mockResponse ?: ExploreAmapResponse(ok = true, cityId = cityId, type = type, pois = emptyList())
        }
    }

    @Test
    fun testFetchPoisSuccessMapping() = runBlocking {
        val mockApi = MockExploreApiService()
        
        // Setup mock Amap JSON nodes: address as array, tel as primitive, biz_ext as object
        val bizExtJson = JsonObject().apply {
            addProperty("rating", "4.9")
            addProperty("cost", "120")
        }
        val addressJson = JsonArray().apply {
            add(JsonPrimitive("100 Forbidden Palace St"))
        }
        val telJson = JsonPrimitive("010-85007000")

        mockApi.mockResponse = ExploreAmapResponse(
            ok = true,
            cityId = "beijing",
            type = "attractions",
            pois = listOf(
                AmapPoiJson(
                    id = "poi_1",
                    name = "Forbidden City",
                    type = "Sights",
                    address = addressJson,
                    tel = telJson,
                    opentime_week = "08:30-17:00",
                    business_area = "Dongcheng",
                    location = "116.397,39.918",
                    biz_ext = bizExtJson,
                    distance = "905",
                )
            )
        )

        val repository = LiveExploreRepository(mockApi)
        val result = repository.getPois("Beijing", ExploreCategory.Attraction)

        assertEquals(1, result.size)
        val attraction = result[0]
        assertEquals("amap-poi_1", attraction.id)
        assertEquals("Forbidden City", attraction.name)
        // v0.3.22 (#47): getPois() now lowercases the city id before mapping,
        // matching the SUPPORTED_CITIES convention (ids are always lowercase).
        assertEquals("beijing", attraction.city)
        assertEquals(ExploreCategory.Attraction, attraction.category)
        assertEquals(4.9, attraction.rating, 0.01)
        assertEquals("¥120", attraction.priceHint)
        assertEquals("100 Forbidden Palace St", attraction.address)
        assertEquals("010-85007000", attraction.phone)
        assertEquals("08:30-17:00", attraction.openingHours)
        assertEquals("https://uri.amap.com/marker?position=116.397,39.918&name=Forbidden City", attraction.mapUrl)
        assertEquals("Amap", attraction.sourceLabel)
        assertEquals(39.918, attraction.coordinates?.lat ?: 0.0, 0.01)
        assertEquals(116.397, attraction.coordinates?.lng ?: 0.0, 0.01)
        // Regression test for the architect-takeover fix: Amap's around-search
        // response carries a real "distance" field (verified live via curl),
        // but the original #47 submission never declared it on AmapPoiJson nor
        // mapped it onto ExplorePoi.distanceMeters — so the "附近" distance
        // label never rendered even when correctly configured.
        assertEquals(905, attraction.distanceMeters)
        assertTrue(repository.isLiveMode("Beijing"))
    }

    @Test
    fun testFetchPoisStayMappingAndBookingCandidate() = runBlocking {
        val mockApi = MockExploreApiService()
        val bizExtJson = JsonObject().apply {
            addProperty("rating", "4.7")
            addProperty("cost", "500")
        }

        mockApi.mockResponse = ExploreAmapResponse(
            ok = true,
            cityId = "shanghai",
            type = "stays",
            pois = listOf(
                AmapPoiJson(
                    id = "poi_stay_1",
                    name = "Shanghai Central Hotel",
                    type = "Hotel",
                    biz_ext = bizExtJson
                )
            )
        )

        val repository = LiveExploreRepository(mockApi)
        val result = repository.getPois("Shanghai", ExploreCategory.Stay)

        assertEquals(1, result.size)
        val stay = result[0]
        assertEquals("¥500/night", stay.priceHint)
        assertEquals(1, stay.bookingCandidates.size)
        
        val candidate = stay.bookingCandidates[0]
        assertEquals("bc-poi_stay_1", candidate.id)
        assertEquals("Trip.com", candidate.provider)
        assertEquals(BookingCandidateKind.Hotel, candidate.kind)
        assertEquals(BookingCandidateStatus.InfoOnly, candidate.status)
        assertEquals("Pre-booking check required", candidate.note)
    }

    @Test
    fun testFetchPoisFailureFallback() = runBlocking {
        // v0.3.22 (#47): the old mock-data fallback was removed as part of the
        // Dianping redesign — a failed live fetch now surfaces an honest empty
        // state (ExploreViewModel shows a "could not load places" notice)
        // instead of silently substituting fake POIs.
        val mockApi = MockExploreApiService().apply { shouldFail = true }
        val repository = LiveExploreRepository(mockApi)

        val result = repository.getPois("Beijing", ExploreCategory.Attraction)

        assertTrue(result.isEmpty())
        assertFalse(repository.isLiveMode("Beijing"))
    }
}
