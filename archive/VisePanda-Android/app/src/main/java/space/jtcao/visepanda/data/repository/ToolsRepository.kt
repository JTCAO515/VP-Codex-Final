package space.jtcao.visepanda.data.repository

import kotlinx.serialization.json.Json
import space.jtcao.visepanda.data.api.ApiConfig
import space.jtcao.visepanda.data.model.AppConfig
import space.jtcao.visepanda.data.model.ToolItem
import java.net.URL

/**
 * Repository for travel tools and app configuration.
 */
class ToolsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    /** Fetch all travel tools (packing list, visa info, phrases, etc.) */
    suspend fun getTools(): List<ToolItem> {
        val url = URL("${ApiConfig.BASE_URL}/api/tools")
        val response = url.readText()
        return json.decodeFromString(response)
    }

    /** Fetch app configuration */
    suspend fun getConfig(): AppConfig {
        val url = URL("${ApiConfig.BASE_URL}/api/config")
        val response = url.readText()
        return json.decodeFromString(response)
    }
}
