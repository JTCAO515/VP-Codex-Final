package space.jtcao.visepanda.domain.model

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val role: String,
    val status: String
)
