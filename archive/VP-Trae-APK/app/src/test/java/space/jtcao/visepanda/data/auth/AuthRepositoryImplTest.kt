package space.jtcao.visepanda.data.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {

    @Test
    fun `repository should expose restored session on init`() = runBlocking {
        val repository = AuthRepositoryImpl(
            authGateway = object : SupabaseAuthGateway {
                override fun restoreSessionOrNull(): SupabaseAuthEnvelope =
                    SupabaseAuthEnvelope("restored", "restored@test.com", "Restored", "user", "active")

                override suspend fun signInWithEmail(
                    email: String,
                    password: String
                ): SupabaseAuthEnvelope = error("unused")

                override suspend fun signUpWithEmail(
                    email: String,
                    password: String,
                    displayName: String?
                ): SupabaseAuthEnvelope = error("unused")

                override suspend fun sendPasswordReset(email: String) = Unit

                override suspend fun updatePassword(password: String) = Unit

                override suspend fun signOut() = Unit
            }
        )

        val user = repository.observeSession().first()

        assertEquals("restored@test.com", user?.email)
    }

    @Test
    fun `login should update observed session`() = runBlocking {
        val repository = AuthRepositoryImpl(
            authGateway = object : SupabaseAuthGateway {
                override fun restoreSessionOrNull(): SupabaseAuthEnvelope? = null

                override suspend fun signInWithEmail(
                    email: String,
                    password: String
                ) = SupabaseAuthEnvelope("1", email, "Alice", "user", "active")

                override suspend fun signUpWithEmail(
                    email: String,
                    password: String,
                    displayName: String?
                ): SupabaseAuthEnvelope = error("unused")

                override suspend fun sendPasswordReset(email: String) = Unit

                override suspend fun updatePassword(password: String) = Unit

                override suspend fun signOut() = Unit
            }
        )

        repository.login("user@test.com", "secret")

        val user = repository.observeSession().first()

        assertEquals("user@test.com", user?.email)
    }

    @Test
    fun `logout should clear observed session`() = runBlocking {
        val repository = AuthRepositoryImpl(
            authGateway = object : SupabaseAuthGateway {
                override fun restoreSessionOrNull(): SupabaseAuthEnvelope? = null

                override suspend fun signInWithEmail(
                    email: String,
                    password: String
                ) = SupabaseAuthEnvelope("1", email, "Alice", "user", "active")

                override suspend fun signUpWithEmail(
                    email: String,
                    password: String,
                    displayName: String?
                ): SupabaseAuthEnvelope = error("unused")

                override suspend fun sendPasswordReset(email: String) = Unit

                override suspend fun updatePassword(password: String) = Unit

                override suspend fun signOut() = Unit
            }
        )

        repository.login("user@test.com", "secret")
        repository.logout()

        val user = repository.observeSession().first()

        assertEquals(null, user)
    }

    @Test
    fun `default auth gateway should fall back to local dev when supabase client is unavailable`() {
        val gateway = createDefaultAuthGateway(
            createClient = { null }
        )

        assertTrue(gateway is LocalDevSupabaseAuthGateway)
    }
}
