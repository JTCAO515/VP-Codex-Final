package space.jtcao.visepanda.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import space.jtcao.visepanda.data.api.ApiConfig
import space.jtcao.visepanda.data.model.City
import space.jtcao.visepanda.data.model.CityDetail
import space.jtcao.visepanda.data.model.CityDetailResponse
import space.jtcao.visepanda.data.model.MapData
import java.net.URL

/**
 * Repository for city and map data — fetched from the VisePanda API.
 *
 * API response formats:
 *   GET /api/cities   → { "cities": { "beijing": {...}, "shanghai": {...} } }
 *   GET /api/cities/x → { "city": { ... } }
 *   GET /api/map      → { "cities": [...] }
 */
class CityRepository {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetch all cities as a flat list.
     * The API returns a map: { cities: { slug: {...}, slug: {...} } }
     */
    suspend fun getCities(): List<Pair<String, City>> {
        val url = URL("${ApiConfig.BASE_URL}/api/cities")
        val response = url.readText()
        val root = json.parseToJsonElement(response).jsonObject
        val citiesObj = root["cities"]?.jsonObject ?: return emptyList()

        return citiesObj.entries.map { (name, element) ->
            val obj = element.jsonObject
            val city = City(
                nameCn = obj["name_cn"]?.jsonPrimitive?.content ?: "",
                province = obj["province"]?.jsonPrimitive?.content ?: "",
                bestSeason = obj["best_season"]?.jsonPrimitive?.content ?: "",
                days = obj["days"]?.jsonPrimitive?.content ?: "",
                vibe = obj["vibe"]?.jsonPrimitive?.content ?: "",
                budgetTip = obj["budget_tip"]?.jsonPrimitive?.content ?: "",
                image = obj["image"]?.jsonPrimitive?.content ?: ""
            )
            name to city
        }
    }

    /**
     * Fetch a single city's full detail.
     * API returns: { city: { ..., food: [...], hotels: {...}, tips: [...], estimate: {...}, map: {...} } }
     */
    suspend fun getCityDetail(city: String): CityDetail {
        val url = URL("${ApiConfig.BASE_URL}/api/cities/$city")
        val response = url.readText()
        val wrapper = json.decodeFromString<CityDetailResponse>(response)
        return wrapper.city
    }

    /** Get city image URL */
    fun getCityImageUrl(cityName: String): String {
        return "${ApiConfig.BASE_URL}/static/img/city-$cityName.jpg"
    }
}
