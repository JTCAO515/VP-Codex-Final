package space.go2china.visepanda.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user") val user: AuthUser
)

data class AuthUser(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String?
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class SignUpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)
