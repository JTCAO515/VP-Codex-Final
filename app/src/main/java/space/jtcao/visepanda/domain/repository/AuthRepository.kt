package space.jtcao.visepanda.domain.repository

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.domain.model.AuthUser

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(email: String, password: String, displayName: String?): Result<AuthUser>
    fun observeSession(): Flow<AuthUser?>
    suspend fun logout()
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updatePassword(password: String): Result<Unit>
}
