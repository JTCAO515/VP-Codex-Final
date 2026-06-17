package space.jtcao.visepanda.data.auth

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SupabaseRecoveryCoordinator {
    private val _pendingRecovery = MutableStateFlow(false)
    val pendingRecovery: StateFlow<Boolean> = _pendingRecovery.asStateFlow()

    fun onIntent(intent: Intent?) {
        if (isRecoveryUrl(intent?.dataString)) {
            _pendingRecovery.value = true
        }
    }

    fun consumeRecovery() {
        _pendingRecovery.value = false
    }

    internal fun isRecoveryUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val normalized = url.lowercase()
        return normalized.contains("type=recovery")
    }
}
