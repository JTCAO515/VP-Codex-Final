package space.jtcao.visepanda.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Map markers returned by GET /api/map.
 */
@Serializable
data class MapMarker(
    @SerialName("name") val name: String,
    @SerialName("name_cn") val nameCn: String = "",
    @SerialName("lat") val lat: Double = 0.0,
    @SerialName("lng") val lng: Double = 0.0,
    @SerialName("vibe") val vibe: String = "",
    @SerialName("days") val days: String = ""
)

/**
 * API response wrapper for `/api/map` endpoint.
 */
@Serializable
data class MapApiResponse(
    @SerialName("cities") val cities: List<MapMarker> = emptyList()
)

/**
 * App configuration returned by GET /api/config.
 */
@Serializable
data class AppConfig(
    @SerialName("version") val version: String = "",
    @SerialName("map_center") val mapCenter: MapCenter? = null
)

/**
 * Default map center coordinates.
 */
@Serializable
data class MapCenter(
    @SerialName("lat") val lat: Double = 35.86,
    @SerialName("lng") val lng: Double = 104.19
)
