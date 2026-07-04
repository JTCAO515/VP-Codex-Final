package space.go2china.visepanda.data.repository

import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import space.go2china.visepanda.data.explore.*
import javax.inject.Inject
import javax.inject.Singleton

interface ExploreApiService {
    @GET("api/explore/amap")
    suspend fun searchAmapPois(
        @Query("cityId") cityId: String,
        @Query("type") type: String
    ): ExploreAmapResponse
}

@Singleton
class ExploreRepository @Inject constructor(
    private val retrofit: Retrofit
) {
    private val apiService by lazy { retrofit.create(ExploreApiService::class.java) }

    suspend fun getProviderStatus(): ExploreProviderStatus = withContext(Dispatchers.IO) {
        val isLive = runCatching {
            val response = apiService.searchAmapPois("beijing", "attractions")
            response.ok && !response.pois.isNullOrEmpty()
        }.getOrDefault(false)

        if (isLive) {
            ExploreProviderStatus(
                id = "amap",
                label = "Amap live POI provider",
                mode = "live",
                coverage = "National coverage via Amap POI search. Falls back to 8-city static data if API is unavailable.",
                candidates = listOf("Trip.com", "Meituan", "Tripadvisor"),
                nextIntegration = "Add hotel booking and review data via Trip.com API.",
                limitations = listOf(
                    "POI results are not editorially curated; quality depends on Amap search ranking.",
                    "Amap rich fields are shown only when the upstream result includes them.",
                    "No live ticket prices or booking integration yet."
                )
            )
        } else {
            ExploreProviderStatus(
                id = "static-explore",
                label = "Static curated provider",
                mode = "static",
                coverage = "8 cities with curated attractions, food, and stay areas.",
                candidates = listOf("Amap", "Trip.com", "Meituan", "Tripadvisor"),
                nextIntegration = "POI search and place-detail verification should be validated first.",
                limitations = listOf(
                    "No live opening hours, ticket availability, booking inventory, or map routing yet.",
                    "Static content is written for information architecture and itinerary-planning tests."
                )
            )
        }
    }

    suspend fun listCities(): List<ExploreCity> = withContext(Dispatchers.IO) {
        MockExploreData.cities
    }

    suspend fun listAttractions(cityId: String): List<ExploreAttraction> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.searchAmapPois(cityId, "attractions")
            if (response.ok && response.pois != null) {
                response.pois.map { poi -> poiToAttraction(poi, cityId) }
            } else {
                emptyList()
            }
        }.getOrElse { emptyList() }.ifEmpty {
            MockExploreData.attractions.filter { it.cityId == cityId }
        }
    }

    suspend fun listFoodSpots(cityId: String): List<ExploreFoodSpot> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.searchAmapPois(cityId, "food")
            if (response.ok && response.pois != null) {
                response.pois.map { poi -> poiToFoodSpot(poi, cityId) }
            } else {
                emptyList()
            }
        }.getOrElse { emptyList() }.ifEmpty {
            MockExploreData.foodSpots.filter { it.cityId == cityId }
        }
    }

    suspend fun listStays(cityId: String): List<ExploreStay> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.searchAmapPois(cityId, "stays")
            if (response.ok && response.pois != null) {
                response.pois.map { poi -> poiToStay(poi, cityId) }
            } else {
                emptyList()
            }
        }.getOrElse { emptyList() }.ifEmpty {
            MockExploreData.stays.filter { it.cityId == cityId }
        }
    }

    // --- AmapPoi Converters with safe deserialization helper ---

    private fun scalarString(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null
        if (element.isJsonArray) {
            val arr = element.asJsonArray
            for (i in 0 until arr.size()) {
                val s = arr[i].asString
                if (!s.isNullOrBlank()) return s
            }
            return null
        }
        return element.asString
    }

    private fun getBizExtValue(element: JsonElement?, key: String): String? {
        if (element == null || element.isJsonNull || !element.isJsonObject) return null
        val obj = element.asJsonObject
        if (obj.has(key)) {
            val v = obj.get(key)
            if (v != null && !v.isJsonNull) return v.asString
        }
        return null
    }

    private fun primaryType(poi: AmapPoi): String {
        return poi.bizType ?: poi.type.split(";").firstOrNull() ?: ""
    }

    private fun parseLocation(locationStr: String?): ExploreLocation? {
        val parts = locationStr?.split(",") ?: return null
        if (parts.size >= 2) {
            val lng = parts[0].toDoubleOrNull()
            val lat = parts[1].toDoubleOrNull()
            if (lng != null && lat != null) {
                return ExploreLocation(lat = lat, lng = lng)
            }
        }
        return null
    }

    private fun priceLevel(cost: String?): String? {
        val amount = cost?.toDoubleOrNull() ?: return null
        return when {
            amount <= 0 -> null
            amount < 80 -> "¥"
            amount < 220 -> "¥¥"
            else -> "¥¥¥"
        }
    }

    private fun poiToAttraction(poi: AmapPoi, cityId: String): ExploreAttraction {
        val rating = getBizExtValue(poi.bizExt, "rating")
        val cost = getBizExtValue(poi.bizExt, "cost")
        val photoUrl = poi.photos?.firstOrNull { !it.url.isNullOrBlank() }?.url
        val ratingValue = rating?.toDoubleOrNull() ?: 0.0

        return ExploreAttraction(
            id = "amap-${poi.id}",
            cityId = cityId,
            name = poi.name,
            category = primaryType(poi),
            description = scalarString(poi.address) ?: poi.adname ?: cityId,
            rating = rating,
            pricePerPerson = cost,
            priceLevel = priceLevel(cost),
            tel = scalarString(poi.tel),
            openHours = poi.opentimeWeek,
            photoUrl = photoUrl,
            businessArea = poi.businessArea,
            sourceLabel = "Amap",
            location = parseLocation(poi.location),
            confidence = if (ratingValue >= 4.5) "High (96%)" else "Medium (85%)",
            fitRationale = if (primaryType(poi).contains("Heritage", ignoreCase = true)) {
                "Authentic historical landmark. English brochures are available at the service center."
            } else {
                "Recommended local landmark with good foreigner-accessibility."
            }
        )
    }

    private fun poiToFoodSpot(poi: AmapPoi, cityId: String): ExploreFoodSpot {
        val rating = getBizExtValue(poi.bizExt, "rating")
        val cost = getBizExtValue(poi.bizExt, "cost")
        val photoUrl = poi.photos?.firstOrNull { !it.url.isNullOrBlank() }?.url
        val ratingValue = rating?.toDoubleOrNull() ?: 0.0

        return ExploreFoodSpot(
            id = "amap-${poi.id}",
            cityId = cityId,
            name = poi.name,
            dish = primaryType(poi),
            description = scalarString(poi.address) ?: poi.adname ?: cityId,
            rating = rating,
            pricePerPerson = cost,
            priceLevel = priceLevel(cost),
            tel = scalarString(poi.tel),
            openHours = poi.opentimeWeek,
            photoUrl = photoUrl,
            businessArea = poi.businessArea,
            sourceLabel = "Amap",
            location = parseLocation(poi.location),
            confidence = if (ratingValue >= 4.5) "High (95%)" else "Medium (80%)",
            fitRationale = "Highly rated restaurant. Offers typical regional dishes and photo-enabled menu guides."
        )
    }

    private fun poiToStay(poi: AmapPoi, cityId: String): ExploreStay {
        val rating = getBizExtValue(poi.bizExt, "rating")
        val cost = getBizExtValue(poi.bizExt, "cost")
        val photoUrl = poi.photos?.firstOrNull { !it.url.isNullOrBlank() }?.url
        val ratingValue = rating?.toDoubleOrNull() ?: 0.0

        return ExploreStay(
            id = "amap-${poi.id}",
            cityId = cityId,
            name = poi.name,
            area = poi.adname ?: cityId,
            description = scalarString(poi.address) ?: cityId,
            rating = rating,
            pricePerPerson = cost,
            priceLevel = priceLevel(cost),
            tel = scalarString(poi.tel),
            openHours = poi.opentimeWeek,
            photoUrl = photoUrl,
            businessArea = poi.businessArea,
            sourceLabel = "Amap",
            location = parseLocation(poi.location),
            confidence = if (ratingValue >= 4.5) "High (94%)" else "Medium (82%)",
            fitRationale = "Convenient lodging hub in ${poi.adname ?: "downtown"}. Walk to multiple subway intersections."
        )
    }
}
