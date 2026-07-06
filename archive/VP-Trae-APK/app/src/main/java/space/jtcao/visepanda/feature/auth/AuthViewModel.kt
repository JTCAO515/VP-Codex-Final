package space.jtcao.visepanda.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.domain.model.AuthUser
import space.jtcao.visepanda.domain.usecase.LoginWithEmailUseCase
import space.jtcao.visepanda.domain.usecase.ObserveSessionUseCase
import space.jtcao.visepanda.domain.usecase.RegisterWithEmailUseCase

class AuthViewModel(
    private val loginWithEmail: LoginWithEmailUseCase,
    private val registerWithEmail: RegisterWithEmailUseCase,
    private val observeSession: ObserveSessionUseCase,
    private val resetPasswordAction: suspend (String) -> Result<Unit> = { Result.success(Unit) },
    private val updatePasswordAction: suspend (String) -> Result<Unit> = { Result.success(Unit) },
    private val logoutAction: suspend () -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSession().collect { user ->
                _uiState.value = _uiState.value.copy(user = user)
            }
        }
    }

    fun login(email: String, password: String) {
        submitAuthAction {
            loginWithEmail(email, password)
        }
    }

    fun register(email: String, password: String, displayName: String?) {
        submitAuthAction {
            registerWithEmail(email, password, displayName)
        }
    }

    fun resetPassword(email: String) {
        submitSimpleAction {
            resetPasswordAction(email)
        }
    }

    fun updatePassword(password: String) {
        submitSimpleAction {
            updatePasswordAction(password)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching { logoutAction() }
                .onSuccess {
                    _uiState.value = AuthUiState(user = null, loading = false, error = null)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = throwable.message
                    )
                }
        }
    }

    private fun submitSimpleAction(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val result = action()
            _uiState.value = result.fold(
                onSuccess = {
                    _uiState.value.copy(loading = false, error = null)
                },
                onFailure = { throwable ->
                    _uiState.value.copy(
                        loading = false,
                        error = throwable.message
                    )
                }
            )
        }
    }

    private fun submitAuthAction(action: suspend () -> Result<AuthUser>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val result = action()
            _uiState.value = result.fold(
                onSuccess = { user ->
                    AuthUiState(user = user, loading = false)
                },
                onFailure = { throwable ->
                    AuthUiState(
                        user = _uiState.value.user,
                        loading = false,
                        error = throwable.message
                    )
                }
            )
        }
    }
}
