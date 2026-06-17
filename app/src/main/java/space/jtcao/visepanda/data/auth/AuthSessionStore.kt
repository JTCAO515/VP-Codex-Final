package space.jtcao.visepanda.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthSessionSnapshot(
    val userId: String,
    val email: String
)

class AuthSessionStore(
    initialSnapshot: AuthSessionSnapshot? = null
) {
    private val snapshot = MutableStateFlow(initialSnapshot)

    fun observe(): StateFlow<AuthSessionSnapshot?> = snapshot.asStateFlow()

    fun update(value: AuthSessionSnapshot?) {
        snapshot.value = value
    }
}
