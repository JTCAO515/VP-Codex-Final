package space.jtcao.visepanda.data.auth

import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import space.jtcao.visepanda.domain.model.AuthUser
import space.jtcao.visepanda.domain.repository.AuthRepository

internal data class SupabaseAuthEnvelope(
    val id: String,
    val email: String,
    val displayName: String?,
    val role: String,
    val status: String
)

internal interface SupabaseAuthGateway {
    fun restoreSessionOrNull(): SupabaseAuthEnvelope?
    suspend fun signInWithEmail(email: String, password: String): SupabaseAuthEnvelope
    suspend fun signUpWithEmail(email: String, password: String, displayName: String?): SupabaseAuthEnvelope
    suspend fun sendPasswordReset(email: String)
    suspend fun updatePassword(password: String)
    suspend fun signOut()
}

internal class LocalDevSupabaseAuthGateway : SupabaseAuthGateway {
    override fun restoreSessionOrNull(): SupabaseAuthEnvelope? = null

    override suspend fun signInWithEmail(email: String, password: String): SupabaseAuthEnvelope {
        require(email.isNotBlank()) { "Email is required." }
        require(password.isNotBlank()) { "Password is required." }

        return SupabaseAuthEnvelope(
            id = "local-dev",
            email = email,
            displayName = null,
            role = "user",
            status = "active"
        )
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): SupabaseAuthEnvelope {
        require(email.isNotBlank()) { "Email is required." }
        require(password.isNotBlank()) { "Password is required." }

        return SupabaseAuthEnvelope(
            id = "local-dev",
            email = email,
            displayName = displayName,
            role = "user",
            status = "pending"
        )
    }

    override suspend fun sendPasswordReset(email: String) {
        require(email.isNotBlank()) { "Email is required." }
    }

    override suspend fun updatePassword(password: String) {
        require(password.isNotBlank()) { "Password is required." }
    }

    override suspend fun signOut() = Unit
}

private fun SupabaseAuthEnvelope.toDomainModel(): AuthUser =
    AuthUser(
        id = id,
        email = email,
        displayName = displayName,
        role = role,
        status = status
    )

internal fun createDefaultAuthGateway(
    createClient: () -> SupabaseClient? = SupabaseClientFactory::createOrNull
): SupabaseAuthGateway = createClient()?.let(::RealSupabaseAuthGateway) ?: LocalDevSupabaseAuthGateway()

class AuthRepositoryImpl(
    private val authGateway: SupabaseAuthGateway = createDefaultAuthGateway()
) : AuthRepository {
    private val session = MutableStateFlow(authGateway.restoreSessionOrNull()?.toDomainModel())

    override suspend fun login(email: String, password: String): Result<AuthUser> =
        runCatching {
            authGateway.signInWithEmail(email, password).toDomainModel()
        }.onSuccess { user ->
            session.value = user
        }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthUser> =
        runCatching {
            authGateway.signUpWithEmail(email, password, displayName).toDomainModel()
        }.onSuccess { user ->
            session.value = user
        }

    override fun observeSession(): Flow<AuthUser?> = session.asStateFlow()

    override suspend fun logout() {
        authGateway.signOut()
        session.value = null
    }

    override suspend fun resetPassword(email: String): Result<Unit> =
        runCatching {
            authGateway.sendPasswordReset(email)
        }

    override suspend fun updatePassword(password: String): Result<Unit> =
        runCatching {
            authGateway.updatePassword(password)
        }
}
