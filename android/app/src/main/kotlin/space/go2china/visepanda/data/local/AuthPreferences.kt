package space.go2china.visepanda.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface AuthPreferences {
    fun saveSession(accessToken: String, refreshToken: String, email: String, userId: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getEmail(): String?
    fun getUserId(): String?
    fun clearSession()
}

@Singleton
class SharedPrefsAuthPreferences @Inject constructor(
    @ApplicationContext context: Context
) : AuthPreferences {
    private val sharedPrefs = context.getSharedPreferences("visepanda_auth_prefs", Context.MODE_PRIVATE)

    override fun saveSession(accessToken: String, refreshToken: String, email: String, userId: String) {
        val encryptedAccessToken = CryptoManager.encrypt(accessToken)
        val encryptedRefreshToken = CryptoManager.encrypt(refreshToken)
        val encryptedEmail = CryptoManager.encrypt(email)
        val encryptedUserId = CryptoManager.encrypt(userId)
        
        sharedPrefs.edit()
            .putString("access_token", encryptedAccessToken)
            .putString("refresh_token", encryptedRefreshToken)
            .putString("user_email", encryptedEmail)
            .putString("user_id", encryptedUserId)
            .apply()
    }

    override fun getAccessToken(): String? {
        val raw = sharedPrefs.getString("access_token", null) ?: return null
        return CryptoManager.decrypt(raw)
    }

    override fun getRefreshToken(): String? {
        val raw = sharedPrefs.getString("refresh_token", null) ?: return null
        return CryptoManager.decrypt(raw)
    }

    override fun getEmail(): String? {
        val raw = sharedPrefs.getString("user_email", null) ?: return null
        return CryptoManager.decrypt(raw)
    }

    override fun getUserId(): String? {
        val raw = sharedPrefs.getString("user_id", null) ?: return null
        return CryptoManager.decrypt(raw)
    }

    override fun clearSession() {
        sharedPrefs.edit().clear().apply()
    }
}
