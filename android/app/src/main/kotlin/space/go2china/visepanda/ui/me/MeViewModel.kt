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
import space.go2china.visepanda.data.repository.AuthRepository
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
    val syncStatus: SyncStatus = SyncStatus.NOT_SIGNED_IN
)

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MeViewModel @Inject constructor(
    tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    private val syncManager: SupabaseSyncManager
) : ViewModel() {

    private val authState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = authRepository.isLoggedIn(),
            userEmail = authRepository.getEmail()
        )
    )

    val uiState: StateFlow<MeUiState> = combine(
        tripRepository.observeActiveTrip(),
        authState,
        syncManager.syncStatus
    ) { trip, auth, sync ->
        MeUiState(
            activeTripTitle = trip?.summary?.title,
            hasCachedTripData = trip?.days?.isNotEmpty() == true,
            isLoggedIn = auth.isLoggedIn,
            userEmail = auth.userEmail,
            isLoading = auth.isLoading,
            errorMessage = auth.errorMessage,
            syncStatus = sync
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
