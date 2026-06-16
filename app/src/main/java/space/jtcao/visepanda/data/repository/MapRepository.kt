package space.jtcao.visepanda.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import space.jtcao.visepanda.data.api.ApiConfig
import space.jtcao.visepanda.data.model.MapMarker
import java.net.URL

/**
 * Repository for map data — coordinates of all cities in China.
 *
 * API: GET /api/map → { cities: [{name, name_cn, lat, lng, vibe, days}, ...] }
 */
class MapRepository {

    private val json = Json { ignoreUnknownKeys = true }

    /** Fetch all city markers with coordinates */
    suspend fun getMarkers(): List<MapMarker> {
        val url = URL("${ApiConfig.BASE_URL}/api/map")
        val response = url.readText()
        val root = json.parseToJsonElement(response).jsonObject
        val citiesArray = root["cities"]?.jsonArray ?: return emptyList()

        return citiesArray.map { element ->
            val obj = element.jsonObject
            MapMarker(
                name = obj["name"]?.jsonPrimitive?.content ?: "",
                nameCn = obj["name_cn"]?.jsonPrimitive?.content ?: "",
                lat = obj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                lng = obj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                vibe = obj["vibe"]?.jsonPrimitive?.content ?: "",
                days = obj["days"]?.jsonPrimitive?.content ?: ""
            )
        }
    }
}
