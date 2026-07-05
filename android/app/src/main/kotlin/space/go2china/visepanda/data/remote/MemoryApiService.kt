package space.go2china.visepanda.data.remote

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query
import space.go2china.visepanda.data.model.MemoryDeleteResponse
import space.go2china.visepanda.data.model.MemoryProfileResponse

interface MemoryApiService {
    @GET("butler/memory/profile")
    suspend fun getProfile(@Query("userId") userId: String): MemoryProfileResponse

    @DELETE("butler/memory/profile")
    suspend fun deleteEntry(
        @Query("userId") userId: String,
        @Query("key") key: String,
        @Query("value") value: String,
    ): MemoryDeleteResponse
}
