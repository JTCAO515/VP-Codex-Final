package space.go2china.visepanda.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import space.go2china.visepanda.data.model.TripState

data class SupabaseUserBody(
    val id: String,
    val email: String
)

data class SupabaseTripInsertBody(
    val id: String,
    val owner_id: String,
    val title: String,
    val status: String
)

data class SupabaseTripPatchBody(
    val title: String? = null,
    val status: String? = null,
    val updated_at: String? = null,
    val current_canvas_version_id: String? = null
)

data class SupabaseCanvasVersionBody(
    val id: String,
    val trip_id: String,
    val canvas: TripState,
    val last_updated_reason: String
)

data class SupabaseMessageBody(
    val id: String,
    val trip_id: String,
    val role: String,
    val content: String,
    val created_at: String
)

interface SupabaseTripApiService {
    @POST("rest/v1/users")
    suspend fun upsertUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body body: SupabaseUserBody
    ): Response<Unit>

    @POST("rest/v1/trips")
    suspend fun insertTrip(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body body: SupabaseTripInsertBody
    ): Response<Unit>

    @PATCH("rest/v1/trips")
    suspend fun patchTrip(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") idFilter: String, // e.g. "eq.xxxx-xxxx-xxxx"
        @Body body: SupabaseTripPatchBody
    ): Response<Unit>

    @POST("rest/v1/canvas_versions")
    suspend fun insertCanvasVersion(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body body: SupabaseCanvasVersionBody
    ): Response<Unit>

    @POST("rest/v1/messages")
    suspend fun insertMessage(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body body: SupabaseMessageBody
    ): Response<Unit>
}
