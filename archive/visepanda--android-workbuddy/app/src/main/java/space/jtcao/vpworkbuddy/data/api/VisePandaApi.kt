package space.jtcao.vpworkbuddy.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import kotlinx.serialization.json.JsonObject

/**
 * VisePanda REST API — Retrofit interface.
 *
 * All endpoints return raw ResponseBody — deserialization is done in repositories
 * to avoid converter compatibility issues (the jakewharton serialization converter
 * has known problems with newer Kotlin versions).
 */
interface VisePandaApi {

    @GET("/api/cities")
    suspend fun getCities(): Response<ResponseBody>

    @GET("/api/cities/{city}")
    suspend fun getCityDetail(@Path("city") city: String): Response<ResponseBody>

    @GET("/api/map")
    suspend fun getMapData(): Response<ResponseBody>

    @GET("/api/tools")
    suspend fun getTools(): Response<ResponseBody>

    @GET("/api/config")
    suspend fun getConfig(): Response<ResponseBody>

    @POST("/api/chat")
    suspend fun chat(@Body body: JsonObject): Response<ResponseBody>
}
