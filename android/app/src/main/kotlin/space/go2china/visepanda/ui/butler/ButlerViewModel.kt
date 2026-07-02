package space.go2china.visepanda.ui.butler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.repository.TripRepository

@HiltViewModel
class ButlerViewModel @Inject constructor(
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val localState = MutableStateFlow(ButlerUiState())

    val uiState: StateFlow<ButlerUiState> = combine(
        localState,
        tripRepository.observeButlerMessages(),
    ) { local, messages ->
        local.copy(messages = messages)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ButlerUiState(),
    )

    fun updateInput(value: String) {
        localState.update { it.copy(input = value, errorMessage = null) }
    }

    fun sendCurrentInput() {
        send(localState.value.input)
    }

    fun sendSuggestion(suggestion: String) {
        send(suggestion)
    }

    private fun send(rawMessage: String) {
        val message = rawMessage.trim()
        if (message.isEmpty() || localState.value.sending) return

        localState.update {
            it.copy(input = "", sending = true, errorMessage = null)
        }

        viewModelScope.launch {
            runCatching { tripRepository.sendButlerMessage(message) }
                .onSuccess { result ->
                    localState.update {
                        it.copy(
                            suggestions = result.suggestions.ifEmpty { ButlerUiState.DEFAULT_SUGGESTIONS },
                            modelLabel = result.modelLabel,
                            sending = false,
                            offlineFallback = result.offlineFallback,
                        )
                    }
                }
                .onFailure { error ->
                    localState.update {
                        it.copy(
                            sending = false,
                            errorMessage = error.message ?: "VisePanda could not send that message.",
                        )
                    }
                }
        }
    }
}
