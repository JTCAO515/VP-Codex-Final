package space.go2china.visepanda.data.repository

import com.google.gson.JsonObject
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
import space.go2china.visepanda.data.model.SignUpOutcome
import space.go2china.visepanda.data.model.SignUpRequest
import space.go2china.visepanda.data.remote.AuthApiService

class AuthRepositoryTest {

    /**
     * [confirmationRequired] mirrors this project's real Supabase config
     * (email confirmation ON): a successful signup returns a flat user JSON
     * object with no access_token, not a session.
     */
    private class MockAuthApiService(
        private val shouldFail: Boolean = false,
        private val confirmationRequired: Boolean = false,
    ) : AuthApiService {
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

        override suspend fun signUp(request: SignUpRequest, apiKey: String): JsonObject {
            if (shouldFail) throw Exception("Sign up api failed")
            val json = JsonObject()
            if (confirmationRequired) {
                json.addProperty("id", "user-api-id")
                json.addProperty("email", request.email)
                json.addProperty("confirmation_sent_at", "2026-07-06T00:00:00Z")
            } else {
                json.addProperty("access_token", "api-access-token")
                json.addProperty("token_type", "bearer")
                json.addProperty("expires_in", 3600)
                json.addProperty("refresh_token", "api-refresh-token")
                val user = JsonObject()
                user.addProperty("id", "user-api-id")
                user.addProperty("email", request.email)
                json.add("user", user)
            }
            return json
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

        override suspend fun getUser(authorization: String, apiKey: String): AuthUser {
            if (shouldFail) throw Exception("Get user api failed")
            return AuthUser(id = "user-api-id", email = "oauth@test.com")
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

        var storedGuestId: String? = null

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

        override fun getOrCreateGuestId(): String {
            storedGuestId?.let { return it }
            val newId = "guest-test-id"
            storedGuestId = newId
            return newId
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
        assertTrue(result.getOrNull() is SignUpOutcome.SignedIn)
        assertEquals("user@test.com", mockPrefs.getEmail())
    }

    @Test
    fun testRealSignUp_confirmationRequired_doesNotSaveSessionOrThrow() = runBlocking {
        // This project has Supabase email confirmation ON — a flat user
        // response with no access_token is the success path, not a decode
        // failure, and must not be treated as signed-in.
        val repository = LiveAuthRepository(
            MockAuthApiService(shouldFail = false, confirmationRequired = true),
            mockPrefs,
        )

        val result = repository.signUp("user@test.com", "password123")
        assertTrue(result.isSuccess)
        val outcome = result.getOrNull()
        assertTrue(outcome is SignUpOutcome.ConfirmationRequired)
        assertEquals("user@test.com", (outcome as SignUpOutcome.ConfirmationRequired).email)
        assertNull(mockPrefs.getAccessToken())
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun testCompleteGoogleSignIn_extractsTokensFromFragmentAndSavesSession() = runBlocking {
        val repository = LiveAuthRepository(MockAuthApiService(shouldFail = false), mockPrefs)

        val callbackUrl = "space.go2china.visepanda://auth-callback#access_token=g-access&refresh_token=g-refresh&expires_in=3600&token_type=bearer"
        val result = repository.completeGoogleSignIn(callbackUrl)

        assertTrue(result.isSuccess)
        assertEquals("g-access", result.getOrNull()?.accessToken)
        assertEquals("oauth@test.com", mockPrefs.getEmail())
        assertTrue(repository.isLoggedIn())
    }

    @Test
    fun testCompleteGoogleSignIn_missingTokens_returnsFailureWithoutSavingSession() = runBlocking {
        val repository = LiveAuthRepository(MockAuthApiService(shouldFail = false), mockPrefs)

        val result = repository.completeGoogleSignIn("space.go2china.visepanda://auth-callback#error=access_denied")

        assertTrue(result.isFailure)
        assertFalse(repository.isLoggedIn())
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

    @Test
    fun currentUserId_signedIn_returnsSignedInUserId() {
        mockPrefs.saveSession("access", "refresh", "user@test.com", "id123")
        val repository = LiveAuthRepository(MockAuthApiService(), mockPrefs)

        assertEquals("id123", repository.currentUserId())
    }

    @Test
    fun currentUserId_signedOut_returnsStableGuestId() {
        val repository = LiveAuthRepository(MockAuthApiService(), mockPrefs)

        val first = repository.currentUserId()
        val second = repository.currentUserId()

        assertEquals(first, second)
        assertEquals("guest-test-id", first)
    }
}
