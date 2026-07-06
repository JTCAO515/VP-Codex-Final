package space.jtcao.visepanda.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.eq
import io.github.jan.supabase.postgrest.result.decodeSingleOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal class RealSupabaseAuthGateway(
    private val client: SupabaseClient
) : SupabaseAuthGateway {
    override fun restoreSessionOrNull(): SupabaseAuthEnvelope? {
        val user = client.auth.currentUserOrNull() ?: return null
        val fallbackStatus = if (user.emailConfirmedAt != null || user.confirmedAt != null) {
            "active"
        } else {
            "pending"
        }

        return SupabaseAuthEnvelope(
            id = user.id,
            email = user.email.orEmpty(),
            displayName = user.userMetadata.displayNameOrNull(),
            role = "user",
            status = fallbackStatus
        )
    }

    override suspend fun signInWithEmail(email: String, password: String): SupabaseAuthEnvelope {
        require(email.isNotBlank()) { "Email is required." }
        require(password.isNotBlank()) { "Password is required." }

        val user = client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        return resolveEnvelope(
            user = user,
            fallbackEmail = email,
            fallbackDisplayName = user.userMetadata.displayNameOrNull(),
            fallbackStatus = "active"
        )
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): SupabaseAuthEnvelope {
        require(email.isNotBlank()) { "Email is required." }
        require(password.isNotBlank()) { "Password is required." }

        val normalizedDisplayName = displayName?.trim()?.takeIf { it.isNotEmpty() }
        val redirectUrl = SupabaseClientFactory.redirectUrlOrNull()
        val user = client.auth.signUpWith(
            provider = Email,
            redirectUrl = redirectUrl
        ) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                normalizedDisplayName?.let { put("display_name", it) }
            }
        } ?: client.auth.currentUserOrNull()
            ?: error("Supabase signup succeeded but no user payload was returned.")

        val fallbackStatus = if (user.emailConfirmedAt != null || user.confirmedAt != null) {
            "active"
        } else {
            "pending"
        }

        return resolveEnvelope(
            user = user,
            fallbackEmail = email,
            fallbackDisplayName = normalizedDisplayName ?: user.userMetadata.displayNameOrNull(),
            fallbackStatus = fallbackStatus
        )
    }

    override suspend fun sendPasswordReset(email: String) {
        require(email.isNotBlank()) { "Email is required." }

        val redirectUrl = SupabaseClientFactory.redirectUrlOrNull()
        if (redirectUrl == null) {
            client.auth.resetPasswordForEmail(email = email)
        } else {
            client.auth.resetPasswordForEmail(
                email = email,
                redirectUrl = redirectUrl
            )
        }
    }

    override suspend fun updatePassword(password: String) {
        require(password.isNotBlank()) { "Password is required." }

        client.auth.updateUser {
            this.password = password
        }
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    private suspend fun resolveEnvelope(
        user: UserInfo,
        fallbackEmail: String,
        fallbackDisplayName: String?,
        fallbackStatus: String
    ): SupabaseAuthEnvelope {
        val profile = fetchUserProfile(user.id)
        return SupabaseAuthEnvelope(
            id = profile?.id ?: user.id,
            email = profile?.email ?: user.email ?: fallbackEmail,
            displayName = profile?.displayName ?: fallbackDisplayName,
            role = profile?.role ?: "user",
            status = profile?.status ?: fallbackStatus
        )
    }

    private suspend fun fetchUserProfile(userId: String): UserProfileRow? =
        runCatching {
            client.from("user_profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<UserProfileRow>()
        }.getOrNull()

    @Serializable
    private data class UserProfileRow(
        @SerialName("id") val id: String,
        @SerialName("email") val email: String,
        @SerialName("display_name") val displayName: String? = null,
        @SerialName("role") val role: String = "user",
        @SerialName("status") val status: String = "pending"
    )

    private fun JsonObject?.displayNameOrNull(): String? =
        this?.get("display_name")
            ?.jsonPrimitive
            ?.contentOrNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
}
