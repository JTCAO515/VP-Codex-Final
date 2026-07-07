package space.go2china.visepanda.ui.butler

import androidx.lifecycle.SavedStateHandle
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
import space.go2china.visepanda.data.repository.TranslateRepository
import space.go2china.visepanda.data.repository.TripRepository

@HiltViewModel
class ButlerViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    // Voice input (real-device feedback, 2026-07-05): reuses the existing STT
    // pipeline from Translate rather than duplicating a second speech-to-text
    // integration — same backend endpoint, just fills the composer instead of
    // auto-translating.
    private val translateRepository: TranslateRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val localState = MutableStateFlow(ButlerUiState())

    init {
        val preFilledMsg = savedStateHandle.get<String>("message")
        if (!preFilledMsg.isNullOrEmpty()) {
            send(preFilledMsg)
            savedStateHandle["message"] = null
        }
    }

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

    fun setRecordingState(recording: Boolean) {
        localState.update { it.copy(isRecording = recording) }
    }

    /**
     * Voice input (real-device feedback, 2026-07-05): transcribes the
     * recording and fills the composer so the traveler can review/edit
     * before sending — unlike Translate's auto-translate-on-transcribe flow,
     * Chat never sends on the user's behalf.
     */
    fun performStt(audioBase64: String) {
        localState.update { it.copy(isTranscribing = true, isRecording = false, errorMessage = null) }
        viewModelScope.launch {
            translateRepository.translateStt(audioBase64, "audio/mpeg", "zh")
                .onSuccess { text ->
                    localState.update { it.copy(isTranscribing = false, input = text) }
                }
                .onFailure { error ->
                    localState.update {
                        it.copy(
                            isTranscribing = false,
                            errorMessage = error.message ?: "Voice input failed. Please try again.",
                        )
                    }
                }
        }
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
