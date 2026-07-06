package space.jtcao.visepanda.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * City summary — from GET /api/cities map values.
 *
 * Each city object in the response map has these fields.
 * The `name` key is the city's slug (e.g. "beijing").
 */
@Serializable
data class City(
    @SerialName("name_cn") val nameCn: String = "",
    @SerialName("province") val province: String = "",
    @SerialName("best_season") val bestSeason: String = "",
    @SerialName("days") val days: String = "",
    @SerialName("vibe") val vibe: String = "",
    @SerialName("highlights") val highlights: List<String> = emptyList(),
    @SerialName("budget_tip") val budgetTip: String = "",
    @SerialName("image") val image: String = ""
)

/**
 * Wrapper for GET /api/cities/{city} response.
 * The API returns everything nested under a "city" key.
 */
@Serializable
data class CityDetailResponse(
    @SerialName("city") val city: CityDetail
)

/**
 * City detail — returned from GET /api/cities/{city}.
 * Contains all fields from City plus nested data.
 */
@Serializable
data class CityDetail(
    @SerialName("name_cn") val nameCn: String = "",
    @SerialName("province") val province: String = "",
    @SerialName("best_season") val bestSeason: String = "",
    @SerialName("days") val days: String = "",
    @SerialName("vibe") val vibe: String = "",
    @SerialName("highlights") val highlights: List<String> = emptyList(),
    @SerialName("budget_tip") val budgetTip: String = "",
    @SerialName("image") val image: String = "",
    @SerialName("estimate") val estimate: PriceEstimate = PriceEstimate(),
    @SerialName("food") val food: List<FoodItem> = emptyList(),
    @SerialName("hotels") val hotels: HotelData = HotelData(),
    @SerialName("tips") val tips: List<TipItem> = emptyList(),
    @SerialName("map") val mapData: MapData = MapData()
)

// ── Nested models ──

@Serializable
data class PriceEstimate(
    @SerialName("budget_daily") val budgetDaily: String = "",
    @SerialName("mid_daily") val midDaily: String = "",
    @SerialName("luxury_daily") val luxuryDaily: String = "",
    @SerialName("flight_avg") val flightAvg: String = "",
    @SerialName("food_avg") val foodAvg: String = ""
)

@Serializable
data class FoodItem(
    @SerialName("name_en") val nameEn: String = "",
    @SerialName("name_cn") val nameCn: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("price_range") val priceRange: String = "",
    @SerialName("must_try") val mustTry: Boolean = false
)

@Serializable
data class HotelData(
    @SerialName("budget") val budget: HotelTier = HotelTier(),
    @SerialName("mid") val mid: HotelTier = HotelTier(),
    @SerialName("luxury") val luxury: HotelTier = HotelTier(),
    @SerialName("tip") val tip: String = ""
)

@Serializable
data class HotelTier(
    @SerialName("range") val range: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("areas") val areas: String = ""
)

@Serializable
data class TipItem(
    @SerialName("en") val en: String = "",
    @SerialName("tip") val tip: String = ""
)

@Serializable
data class MapData(
    @SerialName("lat") val lat: Double = 0.0,
    @SerialName("lng") val lng: Double = 0.0,
    @SerialName("zoom") val zoom: Int = 11,
    @SerialName("pois") val pois: List<PoiItem> = emptyList()
)

@Serializable
data class PoiItem(
    @SerialName("name") val name: String = "",
    @SerialName("name_cn") val nameCn: String = "",
    @SerialName("lat") val lat: Double = 0.0,
    @SerialName("lng") val lng: Double = 0.0,
    @SerialName("type") val type: String = ""
)
