package space.jtcao.visepanda.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import space.jtcao.visepanda.data.api.ApiConfig
import space.jtcao.visepanda.data.model.City
import space.jtcao.visepanda.data.model.CityDetail
import space.jtcao.visepanda.data.model.MapData
import java.net.URL

/**
 * Repository for city and map data — fetched from the VisePanda API.
 *
 * The API returns cities as a JSON map keyed by city name:
 *   { "cities": { "beijing": {...}, "shanghai": {...} } }
 *
 * We convert this to a flat list of [City] objects.
 */
class CityRepository {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetch all cities as a flat list.
     * The API returns a map: { cities: { name: {...}, name: {...} } }
     */
    suspend fun getCities(): List<City> {
        val url = URL("${ApiConfig.BASE_URL}/api/cities")
        val response = url.readText()
        val root = json.parseToJsonElement(response).jsonObject
        val citiesObj = root["cities"]?.jsonObject ?: return emptyList()

        return citiesObj.entries.map { (name, element) ->
            val obj = element.jsonObject
            City(
                name = name,
                nameCn = obj["name_cn"]?.jsonPrimitive?.content ?: "",
                province = obj["province"]?.jsonPrimitive?.content ?: "",
                bestSeason = obj["best_season"]?.jsonPrimitive?.content ?: "",
                days = obj["days"]?.jsonPrimitive?.content ?: "",
                vibe = obj["vibe"]?.jsonPrimitive?.content ?: "",
                highlights = obj["highlights"]?.jsonPrimitive?.content ?: "",
                budgetTip = obj["budget_tip"]?.jsonPrimitive?.content ?: "",
                image = obj["image"]?.jsonPrimitive?.content ?: ""
            )
        }
    }

    /**
     * Fetch a single city's full detail.
     * Returns: { city: {...}, attractions: [...], food: [...], hotels: {...}, tips: [...], estimates: {...} }
     */
    suspend fun getCityDetail(city: String): CityDetail {
        val url = URL("${ApiConfig.BASE_URL}/api/cities/$city")
        val response = url.readText()
        return json.decodeFromString(response)
    }

    /** Fetch map markers (36-city coordinates) */
    suspend fun getMapData(): MapData {
        val url = URL("${ApiConfig.BASE_URL}/api/map")
        val response = url.readText()
        return json.decodeFromString(response)
    }

    /** Build image URL for a city card */
    fun getCityImageUrl(cityName: String): String {
        return "${ApiConfig.BASE_URL}/static/img/city-$cityName.jpg"
    }
}
