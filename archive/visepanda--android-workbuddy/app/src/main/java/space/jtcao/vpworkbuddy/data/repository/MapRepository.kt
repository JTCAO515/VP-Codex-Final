package space.jtcao.vpworkbuddy.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import space.jtcao.vpworkbuddy.data.api.ApiClient
import space.jtcao.vpworkbuddy.data.model.MapApiResponse
import space.jtcao.vpworkbuddy.data.model.MapMarker

class MapRepository {

    private val api = ApiClient.api
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getMarkers(): List<MapMarker> = withContext(Dispatchers.IO) {
        val response = api.getMapData()
        val body = response.body()?.string() ?: throw Exception("Empty response")
        val mapResponse = json.decodeFromString<MapApiResponse>(body)
        mapResponse.cities
    }
}
