package space.jtcao.vpworkbuddy.data.model

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
 * API response wrapper for `/api/cities` endpoint.
 * Returns a map: { slug → City }.
 */
@Serializable
data class CitiesResponse(
    @SerialName("cities") val cities: Map<String, City> = emptyMap()
)

/**
 * API response wrapper for `/api/tools` endpoint.
 * Returns a map: { tool_name → description }.
 */
@Serializable
data class ToolsResponse(
    @SerialName("tools") val tools: Map<String, String> = emptyMap()
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
