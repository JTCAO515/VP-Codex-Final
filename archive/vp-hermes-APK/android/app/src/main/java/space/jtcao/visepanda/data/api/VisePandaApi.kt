package space.jtcao.visepanda.data.api

import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import space.jtcao.visepanda.data.model.AppConfig
import space.jtcao.visepanda.data.model.City
import space.jtcao.visepanda.data.model.CityDetail
import space.jtcao.visepanda.data.model.MapData
import space.jtcao.visepanda.data.model.ToolItem

/**
 * VisePanda REST API — Retrofit interface.
 *
 * All endpoints served from Vercel (WSGI Python backend).
 */
interface VisePandaApi {

    /** List all 36 cities */
    @GET("/api/cities")
    suspend fun getCities(): List<City>

    /** Get city detail with attractions, food, hotels, tips, estimates */
    @GET("/api/cities/{city}")
    suspend fun getCityDetail(@Path("city") city: String): CityDetail

    /** Get full China map data with all city markers */
    @GET("/api/map")
    suspend fun getMapData(): MapData

    /** List all travel tools */
    @GET("/api/tools")
    suspend fun getTools(): List<ToolItem>

    /** Get app configuration */
    @GET("/api/config")
    suspend fun getConfig(): AppConfig

    /**
     * Chat endpoint — returns SSE stream.
     * NOTE: This is NOT called directly via Retrofit.
     * SSE streaming uses SseClient (OkHttp raw) instead.
     */
    @POST("/api/chat")
    suspend fun chat(
        @Body body: JsonObject
    ): retrofit2.Response<okhttp3.ResponseBody>
}
