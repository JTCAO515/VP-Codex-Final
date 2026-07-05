package space.go2china.visepanda.data.repository

import space.go2china.visepanda.data.explore.ExploreCategory
import space.go2china.visepanda.data.explore.ExplorePoi
import space.go2china.visepanda.data.explore.MockExploreData
import space.go2china.visepanda.data.model.BookingCandidate
import space.go2china.visepanda.data.model.BookingCandidateKind
import space.go2china.visepanda.data.model.BookingCandidateStatus
import space.go2china.visepanda.data.model.Coordinates
import space.go2china.visepanda.data.remote.ExploreApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveExploreRepository @Inject constructor(
    private val apiService: ExploreApiService
) : ExploreRepository {

    private val liveStatusCache = mutableMapOf<String, Boolean>()

    override suspend fun getPois(city: String, category: ExploreCategory): List<ExplorePoi> {
        val cityId = city.lowercase()
        val typeParam = when (category) {
            ExploreCategory.Attraction -> "attractions"
            ExploreCategory.Food -> "food"
            ExploreCategory.Stay -> "stays"
        }

        return try {
            val response = apiService.searchAmapPois(cityId, typeParam)
            if (response.ok) {
                liveStatusCache[cityId] = true
                response.pois.map { poi ->
                    val ratingVal = poi.getRating()?.toDoubleOrNull() ?: 0.0
                    val costVal = poi.getCost()?.toIntOrNull() ?: 0
                    
                    val priceHintVal = when (category) {
                        ExploreCategory.Attraction -> if (costVal > 0) "¥$costVal" else "Free"
                        ExploreCategory.Food -> if (costVal > 0) "¥$costVal/person" else "¥"
                        ExploreCategory.Stay -> if (costVal > 0) "¥$costVal/night" else "¥"
                    }

                    val addressVal = poi.getAddressValue()
                    val descriptionVal = addressVal ?: (poi.business_area ?: city)

                    val coords = poi.location?.split(",")?.let { parts ->
                        val lng = parts.getOrNull(0)?.toDoubleOrNull()
                        val lat = parts.getOrNull(1)?.toDoubleOrNull()
                        if (lng != null && lat != null) Coordinates(lat, lng) else null
                    }

                    val mapUrlVal = poi.location?.let { "https://uri.amap.com/marker?position=$it&name=${poi.name}" }

                    // Generate info-only booking candidates for Food & Stay per PRD spec
                    val bookingCandidatesList = mutableListOf<BookingCandidate>()
                    if (category == ExploreCategory.Stay) {
                        bookingCandidatesList.add(
                            BookingCandidate(
                                id = "bc-${poi.id}",
                                kind = BookingCandidateKind.Hotel,
                                label = "Plan reference via Trip.com",
                                provider = "Trip.com",
                                status = BookingCandidateStatus.InfoOnly,
                                note = "Pre-booking check required",
                                priceHint = if (costVal > 0) "¥$costVal" else null
                            )
                        )
                    } else if (category == ExploreCategory.Food) {
                        bookingCandidatesList.add(
                            BookingCandidate(
                                id = "bc-${poi.id}",
                                kind = BookingCandidateKind.Restaurant,
                                label = "Plan reference via Dianping",
                                provider = "Dianping",
                                status = BookingCandidateStatus.InfoOnly,
                                note = "Reservations recommended",
                                priceHint = if (costVal > 0) "¥$costVal" else null
                            )
                        )
                    }

                    ExplorePoi(
                        id = "amap-${poi.id}",
                        name = poi.name,
                        chineseName = poi.name,
                        city = city,
                        category = category,
                        rating = ratingVal,
                        priceHint = priceHintVal,
                        description = descriptionVal,
                        address = addressVal,
                        phone = poi.getTelValue(),
                        openingHours = poi.opentime_week,
                        mapUrl = mapUrlVal,
                        bookingCandidates = bookingCandidatesList,
                        sourceLabel = "Amap",
                        coordinates = coords
                    )
                }
            } else {
                liveStatusCache[cityId] = false
                fallbackMock(city, category)
            }
        } catch (e: Exception) {
            liveStatusCache[cityId] = false
            fallbackMock(city, category)
        }
    }

    override suspend fun isLiveMode(city: String): Boolean {
        return liveStatusCache[city.lowercase()] ?: false
    }

    private fun fallbackMock(city: String, category: ExploreCategory): List<ExplorePoi> {
        return MockExploreData.pois.filter {
            it.city.equals(city, ignoreCase = true) && it.category == category
        }
    }
}
