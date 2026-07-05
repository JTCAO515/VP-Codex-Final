package space.go2china.visepanda.data.repository

import space.go2china.visepanda.data.explore.ExploreCategory
import space.go2china.visepanda.data.explore.ExploreSubcategory
import space.go2china.visepanda.data.explore.ExplorePoi
import space.go2china.visepanda.data.model.BookingCandidate
import space.go2china.visepanda.data.model.BookingCandidateKind
import space.go2china.visepanda.data.model.BookingCandidateStatus
import space.go2china.visepanda.data.model.Coordinates
import space.go2china.visepanda.data.remote.ExploreApiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/** Result from a channel-page fetch, includes pagination. */
data class ExplorePoiPage(
    val pois: List<ExplorePoi>,
    val hasMore: Boolean,
    val isLive: Boolean,
)

@Singleton
class LiveExploreRepository @Inject constructor(
    private val apiService: ExploreApiService
) : ExploreRepository {

    private val liveStatusCache = mutableMapOf<String, Boolean>()

    /** Legacy single-category fetch used by the old 3-tab ViewModel. */
    override suspend fun getPois(city: String, category: ExploreCategory): List<ExplorePoi> {
        val result = fetchChannel(
            cityId = city.lowercase(),
            subcategory = defaultSubcategoryFor(category),
            mode = "city",
            location = null,
            radiusMeters = null,
            page = 1,
        )
        return result.pois
    }

    override suspend fun isLiveMode(city: String): Boolean =
        liveStatusCache[city.lowercase()] ?: false

    /**
     * Dianping-style channel fetch with filters.
     * @param userLat/userLng — non-null triggers around mode
     */
    suspend fun fetchChannel(
        cityId: String,
        subcategory: ExploreSubcategory,
        mode: String = "city",
        location: String? = null,
        radiusMeters: Int? = null,
        sortKey: String = "weight",
        page: Int = 1,
    ): ExplorePoiPage {
        return try {
            val response = apiService.searchAmapPois(
                cityId = cityId,
                type = subcategory.key,
                mode = if (location != null) "around" else "city",
                location = location,
                radius = radiusMeters,
                sort = sortKey,
                page = page,
            )
            if (response.ok) {
                liveStatusCache[cityId] = true
                val pois = response.pois.map { poi ->
                    mapAmapPoi(poi, cityId, subcategory)
                }
                ExplorePoiPage(pois = pois, hasMore = response.hasMore, isLive = true)
            } else {
                liveStatusCache[cityId] = false
                ExplorePoiPage(pois = emptyList(), hasMore = false, isLive = false)
            }
        } catch (e: Exception) {
            liveStatusCache[cityId] = false
            ExplorePoiPage(pois = emptyList(), hasMore = false, isLive = false)
        }
    }

    private fun defaultSubcategoryFor(category: ExploreCategory): ExploreSubcategory = when (category) {
        ExploreCategory.Food -> ExploreSubcategory.FoodAll
        ExploreCategory.Attraction -> ExploreSubcategory.AttractionAll
        ExploreCategory.Stay -> ExploreSubcategory.StayAll
        ExploreCategory.Shopping -> ExploreSubcategory.ShoppingAll
        ExploreCategory.Experience -> ExploreSubcategory.ExperienceAll
    }

    private fun mapAmapPoi(
        poi: space.go2china.visepanda.data.remote.AmapPoiJson,
        city: String,
        subcategory: ExploreSubcategory,
    ): ExplorePoi {
        val ratingVal = poi.getRating()?.toDoubleOrNull() ?: 0.0
        val costVal = poi.getCost()?.toIntOrNull() ?: 0
        val category = subcategory.category

        val priceHintVal = when {
            costVal > 0 -> "¥$costVal${if (category == ExploreCategory.Stay) "/night" else if (category == ExploreCategory.Food) "/person" else ""}"
            category == ExploreCategory.Attraction -> "Free"
            else -> ""
        }

        val addressVal = poi.getAddressValue()

        val coords = poi.location?.split(",")?.let { parts ->
            val lng = parts.getOrNull(0)?.toDoubleOrNull()
            val lat = parts.getOrNull(1)?.toDoubleOrNull()
            if (lng != null && lat != null) Coordinates(lat, lng) else null
        }

        val mapUrlVal = poi.location?.let { "https://uri.amap.com/marker?position=$it&name=${poi.name}" }
        val photoUrl = poi.photos?.firstOrNull()?.url

        val bookingCandidatesList = buildList<BookingCandidate> {
            if (category == ExploreCategory.Stay) {
                add(BookingCandidate(
                    id = "bc-${poi.id}",
                    kind = BookingCandidateKind.Hotel,
                    label = "Plan reference via Trip.com",
                    provider = "Trip.com",
                    status = BookingCandidateStatus.InfoOnly,
                    note = "Pre-booking check required",
                    priceHint = if (costVal > 0) "¥$costVal" else null,
                ))
            } else if (category == ExploreCategory.Food) {
                add(BookingCandidate(
                    id = "bc-${poi.id}",
                    kind = BookingCandidateKind.Restaurant,
                    label = "Plan reference via Dianping",
                    provider = "Dianping",
                    status = BookingCandidateStatus.InfoOnly,
                    note = "Reservations recommended",
                    priceHint = if (costVal > 0) "¥$costVal" else null,
                ))
            }
        }

        return ExplorePoi(
            id = "amap-${poi.id}",
            name = poi.name,
            chineseName = poi.name,
            city = city,
            category = category,
            subcategory = subcategory,
            rating = ratingVal,
            costPerPerson = costVal,
            priceHint = priceHintVal,
            description = addressVal ?: (poi.business_area ?: city),
            address = addressVal,
            phone = poi.getTelValue(),
            openingHours = poi.opentime_week,
            mapUrl = mapUrlVal,
            photoUrl = photoUrl,
            businessArea = poi.business_area,
            bookingCandidates = bookingCandidatesList,
            sourceLabel = "Amap",
            coordinates = coords,
            editorialSummary = poi.editorial?.summary,
            editorialBadges = poi.editorial?.badges,
        )
    }
}
