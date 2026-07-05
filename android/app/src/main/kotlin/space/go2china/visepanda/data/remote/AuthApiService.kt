package space.go2china.visepanda.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import space.go2china.visepanda.data.model.AuthResponse
import space.go2china.visepanda.data.model.LoginRequest
import space.go2china.visepanda.data.model.RefreshTokenRequest
import space.go2china.visepanda.data.model.SignUpRequest

interface AuthApiService {
    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body request: LoginRequest,
        @Header("apikey") apiKey: String,
    ): AuthResponse

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SignUpRequest,
        @Header("apikey") apiKey: String,
    ): AuthResponse

    @POST("auth/v1/token")
    suspend fun refreshToken(
        @Query("grant_type") grantType: String = "refresh_token",
        @Body request: RefreshTokenRequest,
        @Header("apikey") apiKey: String,
    ): AuthResponse

    @POST("auth/v1/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String,
    ): Response<Unit>
}
