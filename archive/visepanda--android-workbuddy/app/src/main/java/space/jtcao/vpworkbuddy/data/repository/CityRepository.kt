package space.jtcao.vpworkbuddy.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import space.jtcao.vpworkbuddy.data.api.ApiClient
import space.jtcao.vpworkbuddy.data.api.ApiConfig
import space.jtcao.vpworkbuddy.data.model.CitiesResponse
import space.jtcao.vpworkbuddy.data.model.City
import space.jtcao.vpworkbuddy.data.model.CityDetail
import space.jtcao.vpworkbuddy.data.model.CityDetailResponse

class CityRepository {

    private val api = ApiClient.api
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCities(): List<Pair<String, City>> = withContext(Dispatchers.IO) {
        val response = api.getCities()
        val body = response.body()?.string() ?: throw Exception("Empty response")
        val citiesResponse = json.decodeFromString<CitiesResponse>(body)
        citiesResponse.cities.entries.map { (slug, city) -> slug to city }
    }

    suspend fun getCityDetail(city: String): CityDetail = withContext(Dispatchers.IO) {
        val response = api.getCityDetail(city)
        val body = response.body()?.string() ?: throw Exception("Empty response")
        val wrapper = json.decodeFromString<CityDetailResponse>(body)
        wrapper.city
    }

    fun getCityImageUrl(cityName: String): String {
        return "${ApiConfig.BASE_URL}/static/img/city-$cityName.jpg"
    }
}
