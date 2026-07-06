package com.visepanda.hermes.data

import android.content.Context
import android.content.SharedPreferences
import com.visepanda.network.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AuthUser(
    val id: String = "",
    val email: String = "",
    val role: String = ""
)

class AuthRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vp_auth", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    /**
     * Save token to SharedPreferences.
     */
    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    /**
     * Get saved token.
     */
    fun getToken(): String? = prefs.getString("auth_token", null)

    /**
     * Save user info.
     */
    fun saveUser(user: AuthUser) {
        prefs.edit()
            .putString("user_id", user.id)
            .putString("user_email", user.email)
            .putString("user_role", user.role)
            .apply()
    }

    /**
     * Get cached user info.
     */
    fun getCachedUser(): AuthUser? {
        val id = prefs.getString("user_id", null) ?: return null
        return AuthUser(
            id = id,
            email = prefs.getString("user_email", "") ?: "",
            role = prefs.getString("user_role", "") ?: ""
        )
    }

    /**
     * Check if user is logged in (has a token locally).
     */
    fun isLoggedIn(): Boolean = getToken() != null

    /**
     * Clear auth data (logout).
     */
    fun logout() {
        prefs.edit().clear().apply()
    }

    /**
     * Register a new account.
     */
    suspend fun register(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()
                val body = json.toRequestBody(JSON_MEDIA)
                val request = Request.Builder()
                    .url("${ApiConfig.BASE_URL}${ApiConfig.AUTH_REGISTER}")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: throw Exception("Empty response")

                if (!response.isSuccessful) {
                    val errJson = JSONObject(responseBody)
                    throw Exception(errJson.optString("error", "Registration failed (${response.code})"))
                }

                responseBody
            }
        }

    /**
     * Login with email and password.
     */
    suspend fun login(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()
                val body = json.toRequestBody(JSON_MEDIA)
                val request = Request.Builder()
                    .url("${ApiConfig.BASE_URL}${ApiConfig.AUTH_LOGIN}")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: throw Exception("Empty response")

                if (!response.isSuccessful) {
                    val errJson = JSONObject(responseBody)
                    throw Exception(errJson.optString("error", "Login failed (${response.code})"))
                }

                responseBody
            }
        }

    /**
     * Verify current token is still valid.
     */
    suspend fun verifyToken(token: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url("${ApiConfig.BASE_URL}${ApiConfig.AUTH_ME}")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: throw Exception("Empty response")

                if (!response.isSuccessful) {
                    throw Exception("Token invalid")
                }

                responseBody
            }
        }

    companion object {
        /**
         * Parse token from login response JSON.
         */
        fun parseToken(responseJson: String): String? {
            return try {
                JSONObject(responseJson).optString("token", "") ?: null
            } catch (e: Exception) { null }
        }

        /**
         * Parse user from auth response JSON.
         */
        fun parseUser(responseJson: String): AuthUser? {
            return try {
                val obj = JSONObject(responseJson)
                val user = obj.optJSONObject("user") ?: return null
                AuthUser(
                    id = user.optString("id", ""),
                    email = user.optString("email", ""),
                    role = user.optString("role", "")
                )
            } catch (e: Exception) { null }
        }
    }
}
