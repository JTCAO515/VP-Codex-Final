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

/**
 * Supabase's /auth/v1/signup returns two different shapes depending on the
 * project's "Confirm email" setting: a full session (this project has email
 * confirmation ON, so this is the disabled-confirmation fallback) or a flat
 * user object with no access_token — the latter is the success path here,
 * not a decode failure, mirrors iOS SupabaseAuthClient.SupabaseSignUpResult.
 */
sealed class SignUpOutcome {
    data class SignedIn(val response: AuthResponse) : SignUpOutcome()
    data class ConfirmationRequired(val email: String) : SignUpOutcome()
}

/** Supabase GoTrue error body shape, mirrors iOS SupabaseAuthError. */
data class SupabaseAuthError(
    @SerializedName("error") val error: String? = null,
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("error_description") val errorDescription: String? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("message") val message: String? = null,
) {
    val displayMessage: String
        get() = if (errorCode == "over_email_send_rate_limit") {
            "Too many signup emails were sent recently. Please wait a while and try again, or sign in if you already created this account."
        } else {
            message ?: msg ?: errorDescription ?: error ?: "Authentication failed"
        }
}

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
