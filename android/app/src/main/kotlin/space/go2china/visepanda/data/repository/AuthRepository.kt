package space.go2china.visepanda.data.repository

import space.go2china.visepanda.data.local.AuthPreferences
import space.go2china.visepanda.data.model.AuthResponse
import space.go2china.visepanda.data.model.AuthUser
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
}

@Singleton
class LiveAuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        if (SupabaseConfig.MOCK_AUTH_ENABLED) {
            if (!email.contains("@")) {
                return Result.failure(IllegalArgumentException("Invalid email address"))
            }
            if (password.length < 6) {
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
            }
            val mockUser = AuthUser(
                id = "mock-uuid-12345",
                email = email
            )
            val mockRes = AuthResponse(
                accessToken = "mock-access-token",
                tokenType = "bearer",
                expiresIn = 3600,
                refreshToken = "mock-refresh-token",
                user = mockUser
            )
            authPreferences.saveSession(
                accessToken = mockRes.accessToken,
                refreshToken = mockRes.refreshToken,
                email = email,
                userId = mockUser.id
            )
            return Result.success(mockRes)
        }

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
        }
    }

    override suspend fun signUp(email: String, password: String): Result<AuthResponse> {
        if (SupabaseConfig.MOCK_AUTH_ENABLED) {
            if (!email.contains("@")) {
                return Result.failure(IllegalArgumentException("Invalid email address"))
            }
            if (password.length < 6) {
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
            }
            val mockUser = AuthUser(
                id = "mock-uuid-12345",
                email = email
            )
            val mockRes = AuthResponse(
                accessToken = "mock-access-token",
                tokenType = "bearer",
                expiresIn = 3600,
                refreshToken = "mock-refresh-token",
                user = mockUser
            )
            authPreferences.saveSession(
                accessToken = mockRes.accessToken,
                refreshToken = mockRes.refreshToken,
                email = email,
                userId = mockUser.id
            )
            return Result.success(mockRes)
        }

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

    override suspend fun logout(): Result<Unit> {
        val accessToken = authPreferences.getAccessToken()
        if (SupabaseConfig.MOCK_AUTH_ENABLED || accessToken == null) {
            authPreferences.clearSession()
            return Result.success(Unit)
        }

        return runCatching {
            authApiService.logout(
                authorization = "Bearer $accessToken",
                apiKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            Unit
        }.also {
            authPreferences.clearSession()
        }
    }

    override fun getEmail(): String? {
        return authPreferences.getEmail()
    }

    override fun isLoggedIn(): Boolean {
        return authPreferences.getAccessToken() != null
    }

    override fun clearSession() {
        authPreferences.clearSession()
    }
}
