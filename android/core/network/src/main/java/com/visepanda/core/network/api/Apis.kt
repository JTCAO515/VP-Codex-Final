package com.visepanda.core.network.api

import com.visepanda.core.network.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<Unit>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<Unit>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Unit>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponse>
}

interface DestinationApi {
    @GET("api/destinations")
    suspend fun listDestinations(
        @Query("search") search: String? = null,
        @Query("tag") tag: String? = null,
        @Query("page") page: Int = 1,
    ): Response<DestinationListResponse>

    @GET("api/destinations/{id}")
    suspend fun getDestination(@Path("id") id: String): Response<DestinationDetail>
}

interface ChatApi {
    @POST("api/chat/message")
    @Streaming
    suspend fun sendMessage(@Body body: ChatRequest): Response<okhttp3.ResponseBody>
}

interface TripApi {
    @GET("api/trips")
    suspend fun listTrips(): Response<TripListResponse>

    @POST("api/trips")
    suspend fun createTrip(@Body body: TripCreateRequest): Response<TripResponse>

    @DELETE("api/trips/{id}")
    suspend fun deleteTrip(@Path("id") id: String): Response<Unit>
}
