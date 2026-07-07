package space.go2china.visepanda.data.repository

import retrofit2.HttpException
import space.go2china.visepanda.data.local.AuthPreferences
import space.go2china.visepanda.data.model.AuthResponse
import space.go2china.visepanda.data.model.LoginRequest
import space.go2china.visepanda.data.model.SignUpOutcome
import space.go2china.visepanda.data.model.SignUpRequest
import space.go2china.visepanda.data.model.SupabaseAuthError
import space.go2china.visepanda.data.remote.AuthApiService
import space.go2china.visepanda.data.remote.SupabaseConfig
import space.go2china.visepanda.data.serialization.TripJson
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun signUp(email: String, password: String): Result<SignUpOutcome>
    suspend fun logout(): Result<Unit>
    fun getEmail(): String?
    fun isLoggedIn(): Boolean
    fun clearSession()
    /** The signed-in Supabase user id, or a stable local guest id if signed out. */
    fun currentUserId(): String
    /** Supabase GoTrue authorize URL for the Google provider; open in a Custom Tab. */
    fun googleOAuthUrl(): String
    /**
     * Completes the Google sign-in flow from the deep-link callback URL that
     * the Custom Tab redirects to (see [googleOAuthUrl]) — extracts the
     * Supabase session tokens from the callback and fetches the real user.
     */
    suspend fun completeGoogleSignIn(callbackUrl: String): Result<AuthResponse>
}

@Singleton
class LiveAuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    /**
     * Sign in with email + password via Supabase GoTrue REST.
     * Persists access/refresh tokens to EncryptedSharedPreferences on success.
     * On 401, clears the stale session so the UI can prompt re-login.
     */
    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return supabaseCall {
            authApiService.login(
                request = LoginRequest(email, password),
                apiKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
        }.onSuccess { response ->
            authPreferences.saveSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                email = response.user.email.orEmpty(),
                userId = response.user.id
            )
        }
    }

    /**
     * Register a new account via Supabase GoTrue REST.
     *
     * This project has email confirmation ON, so a successful signup returns
     * a flat user object with no access_token instead of a session —
     * [SignUpOutcome.ConfirmationRequired] is the success path for that case,
     * not a parse failure. Only [SignUpOutcome.SignedIn] persists a session.
     */
    override suspend fun signUp(email: String, password: String): Result<SignUpOutcome> {
        return supabaseCall {
            authApiService.signUp(
                request = SignUpRequest(email, password),
                apiKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
        }.mapCatching { json ->
            if (json.has("access_token")) {
                val response = TripJson.gson.fromJson(json, AuthResponse::class.java)
                authPreferences.saveSession(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    email = response.user.email.orEmpty(),
                    userId = response.user.id
                )
                SignUpOutcome.SignedIn(response)
            } else {
                val confirmedEmail = json.get("email")?.takeIf { !it.isJsonNull }?.asString ?: email
                SignUpOutcome.ConfirmationRequired(confirmedEmail)
            }
        }
    }

    /**
     * Sign out — calls Supabase GoTrue /auth/v1/logout, then clears local session.
     * Local session is always cleared even if the network call fails.
     */
    override suspend fun logout(): Result<Unit> {
        val accessToken = authPreferences.getAccessToken()
        return if (accessToken != null) {
            runCatching {
                authApiService.logout(
                    authorization = "Bearer $accessToken",
                    apiKey = SupabaseConfig.SUPABASE_ANON_KEY
                )
                Unit
            }.also {
                authPreferences.clearSession()
            }
        } else {
            authPreferences.clearSession()
            Result.success(Unit)
        }
    }

    override fun getEmail(): String? = authPreferences.getEmail()

    override fun isLoggedIn(): Boolean = authPreferences.getAccessToken() != null

    override fun clearSession() = authPreferences.clearSession()

    override fun currentUserId(): String =
        authPreferences.getUserId() ?: authPreferences.getOrCreateGuestId()

    override fun googleOAuthUrl(): String {
        val redirectTo = URLEncoder.encode(GOOGLE_CALLBACK_URL, "UTF-8")
        return "${SupabaseConfig.SUPABASE_URL}/auth/v1/authorize?provider=google&redirect_to=$redirectTo"
    }

    override suspend fun completeGoogleSignIn(callbackUrl: String): Result<AuthResponse> {
        val params = parseCallbackParams(callbackUrl)
        val accessToken = params["access_token"]
        val refreshToken = params["refresh_token"]
        if (accessToken == null || refreshToken == null) {
            return Result.failure(
                Exception(params["error_description"] ?: "Google OAuth did not return a Supabase session.")
            )
        }
        return supabaseCall {
            authApiService.getUser(
                authorization = "Bearer $accessToken",
                apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
            )
        }.map { user ->
            AuthResponse(
                accessToken = accessToken,
                tokenType = params["token_type"] ?: "bearer",
                expiresIn = params["expires_in"]?.toLongOrNull() ?: 3600L,
                refreshToken = refreshToken,
                user = user,
            )
        }.onSuccess { response ->
            authPreferences.saveSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                email = response.user.email.orEmpty(),
                userId = response.user.id
            )
        }
    }

    /** Runs a Supabase REST call, turning error bodies into [SupabaseAuthError.displayMessage]. */
    private suspend fun <T> supabaseCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: HttpException) {
            val parsed = e.response()?.errorBody()?.string()?.let { body ->
                runCatching { TripJson.gson.fromJson(body, SupabaseAuthError::class.java) }.getOrNull()
            }
            Result.failure(Exception(parsed?.displayMessage ?: e.message()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Extracts query + fragment params from a Supabase OAuth redirect URL. */
    private fun parseCallbackParams(callbackUrl: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        fun parseSection(section: String) {
            if (section.isEmpty()) return
            section.split("&").forEach { pair ->
                val idx = pair.indexOf("=")
                if (idx > 0) {
                    val key = pair.substring(0, idx)
                    val value = runCatching { URLDecoder.decode(pair.substring(idx + 1), "UTF-8") }
                        .getOrDefault(pair.substring(idx + 1))
                    params[key] = value
                }
            }
        }
        val afterScheme = callbackUrl.substringAfter("://", "")
        parseSection(afterScheme.substringAfter("?", "").substringBefore("#"))
        parseSection(callbackUrl.substringAfter("#", ""))
        return params
    }

    private companion object {
        const val GOOGLE_CALLBACK_URL = "space.go2china.visepanda://auth-callback"
    }
}
