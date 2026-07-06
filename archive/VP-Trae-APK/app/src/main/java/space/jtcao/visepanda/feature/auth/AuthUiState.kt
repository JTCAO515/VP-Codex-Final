package space.jtcao.visepanda.feature.auth

import space.jtcao.visepanda.domain.model.AuthUser

data class AuthUiState(
    val user: AuthUser? = null,
    val loading: Boolean = false,
    val error: String? = null
)
