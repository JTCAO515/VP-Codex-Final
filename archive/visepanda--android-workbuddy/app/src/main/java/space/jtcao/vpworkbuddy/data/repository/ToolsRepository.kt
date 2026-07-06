package space.jtcao.vpworkbuddy.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import space.jtcao.vpworkbuddy.data.api.ApiClient
import space.jtcao.vpworkbuddy.data.model.AppConfig
import space.jtcao.vpworkbuddy.data.model.ToolsResponse

class ToolsRepository {

    private val api = ApiClient.api
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getTools(): Map<String, String> = withContext(Dispatchers.IO) {
        val response = api.getTools()
        val body = response.body()?.string() ?: throw Exception("Empty response")
        val toolsResponse = json.decodeFromString<ToolsResponse>(body)
        toolsResponse.tools
    }

    suspend fun getConfig(): AppConfig = withContext(Dispatchers.IO) {
        val response = api.getConfig()
        val body = response.body()?.string() ?: throw Exception("Empty response")
        json.decodeFromString<AppConfig>(body)
    }
}
