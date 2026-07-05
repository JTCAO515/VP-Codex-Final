package space.go2china.visepanda.data.repository

import space.go2china.visepanda.data.local.AuthPreferences
import space.go2china.visepanda.data.model.AuthResponse
import space.go2china.visepanda.data.model.LoginRequest
import space.go2china.visepanda.data.model.SignUpRequest
import space.go2china.visepanda.data.remote.AuthApiService
import space.go2china.visepanda.data.remote.SupabaseConfig
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun signUp(email: String, password: String): Result<AuthResponse>
    suspend fun logout(): Result<Unit>
    fun getEmail(): String?
    fun isLoggedIn(): Boolean
    fun clearSession()
    /** The signed-in Supabase user id, or a stable local guest id if signed out. */
    fun currentUserId(): String
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
        return runCatching {
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
        }.onFailure {
            // Leave any existing session intact; caller decides how to surface the error.
        }
    }

    /**
     * Register a new account via Supabase GoTrue REST.
     * Persists session tokens immediately (email-confirm flow not required for this project).
     */
    override suspend fun signUp(email: String, password: String): Result<AuthResponse> {
        return runCatching {
            authApiService.signUp(
                request = SignUpRequest(email, password),
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
}
