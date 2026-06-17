package space.jtcao.visepanda.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import space.jtcao.visepanda.data.api.ApiConfig
import space.jtcao.visepanda.data.model.MapMarker
import java.net.URL

/**
 * Repository for map data — coordinates of all cities in China.
 *
 * API: GET /api/map → { cities: { cityName: { lat, lng } } }
 */
class MapRepository {

    /** Fetch all city markers with coordinates */
    suspend fun getMarkers(): List<MapMarker> {
        val url = URL("${ApiConfig.BASE_URL}/api/map")
        val response = url.readText()
        return parseMarkers(response)
    }

    companion object {
        fun parseMarkersForTest(raw: String): List<MapMarker> = parseMarkers(raw)

        private fun parseMarkers(raw: String): List<MapMarker> {
            val json = Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(raw).jsonObject
            val citiesObject = root["cities"]?.jsonObject ?: return emptyList()

            return citiesObject.entries.map { (name, element) ->
                val obj = element.jsonObject
                MapMarker(
                    name = name,
                    nameCn = name.replaceFirstChar { it.uppercase() },
                    lat = obj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                    lng = obj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                    vibe = "",
                    days = ""
                )
            }
        }
    }
}
