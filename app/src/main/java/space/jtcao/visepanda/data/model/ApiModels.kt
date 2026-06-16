package space.jtcao.visepanda.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Map markers from GET /api/map.
 *
 * API returns dict format:
 *   {"cities": {"beijing": {"lat": 39.9, "lng": 116.4, "name": "北京"}, ...}}
 *
 * MapRepository parses this into List<MapMarker>.
 */
@Serializable
data class MapMarker(
    val key: String = "",
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

/**
 * Map center used as default camera position.
 */
@Serializable
data class MapCenter(
    @SerialName("lat") val lat: Double = 35.86,
    @SerialName("lng") val lng: Double = 104.19,
    @SerialName("zoom") val zoom: Int = 5,
)

/**
 * App configuration from GET /api/config.
 */
@Serializable
data class AppConfig(
    @SerialName("version") val version: String = "",
    @SerialName("map_center") val mapCenter: MapCenter = MapCenter(),
)
