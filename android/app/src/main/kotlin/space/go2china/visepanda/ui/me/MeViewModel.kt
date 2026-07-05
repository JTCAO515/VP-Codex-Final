package space.go2china.visepanda.ui.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.model.MemoryEntry
import space.go2china.visepanda.data.repository.AuthRepository
import space.go2china.visepanda.data.repository.MemoryRepository
import space.go2china.visepanda.data.repository.TripRepository
import space.go2china.visepanda.data.repository.SyncStatus
import space.go2china.visepanda.data.repository.SupabaseSyncManager


data class MeUiState(
    val activeTripTitle: String? = null,
    val hasCachedTripData: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val syncStatus: SyncStatus = SyncStatus.NOT_SIGNED_IN,
    val memoryEntries: List<MemoryEntry> = emptyList(),
    val memoryLoading: Boolean = false,
    val memoryError: String? = null,
    val memoryDeletingKey: String? = null,
)

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class MemoryUiState(
    val entries: List<MemoryEntry> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val deletingKey: String? = null,
)

@HiltViewModel
class MeViewModel @Inject constructor(
    tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    private val memoryRepository: MemoryRepository,
    private val syncManager: SupabaseSyncManager
) : ViewModel() {

    private val authState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = authRepository.isLoggedIn(),
            userEmail = authRepository.getEmail()
        )
    )

    private val memoryState = MutableStateFlow(MemoryUiState())

    init {
        loadMemoryProfile(force = false)
    }

    val uiState: StateFlow<MeUiState> = combine(
        tripRepository.observeActiveTrip(),
        authState,
        syncManager.syncStatus,
        memoryState,
    ) { trip, auth, sync, memory ->
        MeUiState(
            activeTripTitle = trip?.summary?.title,
            hasCachedTripData = trip?.days?.isNotEmpty() == true,
            isLoggedIn = auth.isLoggedIn,
            userEmail = auth.userEmail,
            isLoading = auth.isLoading,
            errorMessage = auth.errorMessage,
            syncStatus = sync,
            memoryEntries = memory.entries,
            memoryLoading = memory.loading,
            memoryError = memory.error,
            memoryDeletingKey = memory.deletingKey,
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MeUiState(
            isLoggedIn = authRepository.isLoggedIn(),
            userEmail = authRepository.getEmail(),
            syncStatus = syncManager.syncStatus.value
        )
    )

    /**
     * AI Profile (Issue #85, mirrors iOS memoryProfileSection) — loads the
     * memory entries for the current user (signed-in id, or a stable local
     * guest id before sign-in, same fallback as iOS's currentUserId).
     */
    fun loadMemoryProfile(force: Boolean) {
        val current = memoryState.value
        if (current.loading || (!force && (current.entries.isNotEmpty() || current.error != null))) return

        memoryState.value = current.copy(loading = true, error = null)
        viewModelScope.launch {
            memoryRepository.fetchProfile(authRepository.currentUserId())
                .onSuccess { entries ->
                    memoryState.value = memoryState.value.copy(loading = false, entries = entries, error = null)
                }
                .onFailure { error ->
                    memoryState.value = memoryState.value.copy(
                        loading = false,
                        error = error.message ?: "Could not reach /butler/memory/profile",
                    )
                }
        }
    }

    fun deleteMemoryEntry(entry: MemoryEntry) {
        memoryState.value = memoryState.value.copy(deletingKey = entry.key)
        viewModelScope.launch {
            memoryRepository.deleteEntry(authRepository.currentUserId(), entry.key, entry.value)
                .onSuccess {
                    memoryState.value = memoryState.value.copy(
                        deletingKey = null,
                        entries = memoryState.value.entries.filterNot { it.key == entry.key && it.value == entry.value },
                    )
                }
                .onFailure { error ->
                    memoryState.value = memoryState.value.copy(
                        deletingKey = null,
                        error = error.message ?: "Could not delete memory entry",
                    )
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authState.value = authState.value.copy(isLoading = true, errorMessage = null)
            authRepository.login(email, password)
                .onSuccess {
                    authState.value = authState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userEmail = it.user.email
                    )
                    syncManager.triggerSync()
                }
                .onFailure {
                    authState.value = authState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Login failed"
                    )
                }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            authState.value = authState.value.copy(isLoading = true, errorMessage = null)
            authRepository.signUp(email, password)
                .onSuccess {
                    authState.value = authState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userEmail = it.user.email
                    )
                    syncManager.triggerSync()
                }
                .onFailure {
                    authState.value = authState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Sign up failed"
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authState.value = authState.value.copy(isLoading = true)
            authRepository.logout()
            syncManager.clearSyncState()
            authState.value = AuthUiState()
        }
    }

    fun clearError() {
        authState.value = authState.value.copy(errorMessage = null)
    }
}
