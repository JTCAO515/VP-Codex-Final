package space.jtcao.visepanda.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import space.jtcao.visepanda.data.api.ApiConfig
import space.jtcao.visepanda.data.model.MapCenter
import space.jtcao.visepanda.data.model.MapMarker

/**
 * Repository for map data.
 *
 * API returns dict format:
 *   GET /api/map → {"cities": {"beijing": {"lat": 39.9, "lng": 116.4, "name": "北京"}, ...}}
 *
 * This repo parses the nested JSON object into a list of MapMarker.
 */
class MapRepository {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getMarkers(): List<MapMarker> {
        return try {
            val url = "${ApiConfig.BASE_URL}/api/map"
            val response = URL(url).readText()
            parseMarkers(response)
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackMarkers()
        }
    }

    private fun parseMarkers(jsonString: String): List<MapMarker> {
        val root = json.parseToJsonElement(jsonString).jsonObject
        val citiesObj = root["cities"]?.jsonObject ?: return getFallbackMarkers()

        return citiesObj.entries.mapNotNull { (key, value) ->
            val obj = value.jsonObject
            try {
                MapMarker(
                    key = key,
                    name = obj["name"]?.jsonPrimitive?.content ?: key,
                    lat = obj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null,
                    lng = obj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getFallbackMarkers(): List<MapMarker> = listOf(
        MapMarker(key = "beijing", name = "北京", lat = 39.9042, lng = 116.4074),
        MapMarker(key = "shanghai", name = "上海", lat = 31.2304, lng = 121.4737),
        MapMarker(key = "guangzhou", name = "广州", lat = 23.1291, lng = 113.2644),
        MapMarker(key = "shenzhen", name = "深圳", lat = 22.5431, lng = 114.0579),
        MapMarker(key = "chengdu", name = "成都", lat = 30.5728, lng = 104.0668),
        MapMarker(key = "chongqing", name = "重庆", lat = 29.4316, lng = 106.9123),
        MapMarker(key = "xian", name = "西安", lat = 34.3416, lng = 108.9398),
    )
}
