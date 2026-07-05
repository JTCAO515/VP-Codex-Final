package space.go2china.visepanda.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import space.go2china.visepanda.data.local.AuthPreferences
import space.go2china.visepanda.data.model.AuthResponse
import space.go2china.visepanda.data.model.AuthUser
import space.go2china.visepanda.data.model.LoginRequest
import space.go2china.visepanda.data.model.RefreshTokenRequest
import space.go2china.visepanda.data.model.SignUpRequest
import space.go2china.visepanda.data.remote.AuthApiService

class AuthRepositoryTest {

    private class MockAuthApiService(private val shouldFail: Boolean = false) : AuthApiService {
        override suspend fun login(
            grantType: String,
            request: LoginRequest,
            apiKey: String
        ): AuthResponse {
            if (shouldFail) throw Exception("Login api failed")
            return AuthResponse(
                accessToken = "api-access-token",
                tokenType = "bearer",
                expiresIn = 3600,
                refreshToken = "api-refresh-token",
                user = AuthUser(id = "user-api-id", email = request.email)
            )
        }

        override suspend fun signUp(request: SignUpRequest, apiKey: String): AuthResponse {
            if (shouldFail) throw Exception("Sign up api failed")
            return AuthResponse(
                accessToken = "api-access-token",
                tokenType = "bearer",
                expiresIn = 3600,
                refreshToken = "api-refresh-token",
                user = AuthUser(id = "user-api-id", email = request.email)
            )
        }

        override suspend fun refreshToken(
            grantType: String,
            request: RefreshTokenRequest,
            apiKey: String
        ): AuthResponse {
            if (shouldFail) throw Exception("Refresh api failed")
            return AuthResponse(
                accessToken = "new-access-token",
                tokenType = "bearer",
                expiresIn = 3600,
                refreshToken = "new-refresh-token",
                user = AuthUser(id = "user-api-id", email = "test@example.com")
            )
        }

        override suspend fun logout(authorization: String, apiKey: String): retrofit2.Response<Unit> {
            if (shouldFail) throw Exception("Logout api failed")
            return retrofit2.Response.success(Unit)
        }
    }

    private class MockAuthPreferences : AuthPreferences {
        var storedAccessToken: String? = null
        var storedRefreshToken: String? = null
        var storedEmail: String? = null
        var storedUserId: String? = null

        override fun saveSession(accessToken: String, refreshToken: String, email: String, userId: String) {
            this.storedAccessToken = accessToken
            this.storedRefreshToken = refreshToken
            this.storedEmail = email
            this.storedUserId = userId
        }

        override fun getAccessToken(): String? = storedAccessToken
        override fun getRefreshToken(): String? = storedRefreshToken
        override fun getEmail(): String? = storedEmail
        override fun getUserId(): String? = storedUserId

        override fun clearSession() {
            storedAccessToken = null
            storedRefreshToken = null
            storedEmail = null
            storedUserId = null
        }
    }

    private lateinit var mockPrefs: MockAuthPreferences

    @Before
    fun setUp() {
        mockPrefs = MockAuthPreferences()
    }

    @Test
    fun testRealLogin_success() = runBlocking {
        val repository = LiveAuthRepository(MockAuthApiService(shouldFail = false), mockPrefs)
        
        val result = repository.login("user@test.com", "password123")
        assertTrue(result.isSuccess)
        
        val response = result.getOrNull()
        assertEquals("api-access-token", response?.accessToken)
        assertEquals("user@test.com", mockPrefs.getEmail())
        assertTrue(repository.isLoggedIn())
    }

    @Test
    fun testRealLogin_failure() = runBlocking {
        val repository = LiveAuthRepository(MockAuthApiService(shouldFail = true), mockPrefs)
        
        val result = repository.login("user@test.com", "password123")
        assertTrue(result.isFailure)
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun testRealSignUp_success() = runBlocking {
        val repository = LiveAuthRepository(MockAuthApiService(shouldFail = false), mockPrefs)
        
        val result = repository.signUp("user@test.com", "password123")
        assertTrue(result.isSuccess)
        assertEquals("user@test.com", mockPrefs.getEmail())
    }

    @Test
    fun testRealLogout_clearsSession() = runBlocking {
        mockPrefs.saveSession("access", "refresh", "user@test.com", "id123")
        
        val repository = LiveAuthRepository(MockAuthApiService(), mockPrefs)
        
        assertTrue(repository.isLoggedIn())
        val result = repository.logout()
        assertTrue(result.isSuccess)
        assertFalse(repository.isLoggedIn())
        assertNull(mockPrefs.getAccessToken())
    }
}
