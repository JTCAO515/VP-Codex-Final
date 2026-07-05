package space.go2china.visepanda.ui.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.model.Phrase
import space.go2china.visepanda.data.model.SupportedLanguages
import space.go2china.visepanda.data.model.TranslateResult
import space.go2china.visepanda.data.repository.TranslateRepository
import javax.inject.Inject

data class TranslateUiState(
    val input: String = "",
    val fromLanguage: String = "en",
    val toLanguage: String = "zh",
    val translating: Boolean = false,
    val translationResult: TranslateResult? = null,
    val errorMessage: String? = null,
    val phrases: List<Phrase> = emptyList(),
    val isProcessing: Boolean = false,
    val isRecording: Boolean = false,
    val permissionError: String? = null,
    /** Set when backend TTS returns a playable URL; Screen plays it then clears this. */
    val ttsAudioUrl: String? = null,
    /** Set when backend TTS fails; Screen falls back to the local system TTS engine then clears this. */
    val ttsFallbackText: String? = null,
    val ttsFallbackLanguageCode: String? = null,
)

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val translateRepository: TranslateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslateUiState())
    val uiState: StateFlow<TranslateUiState> = _uiState.asStateFlow()

    init {
        loadPhrases()
    }

    private fun loadPhrases() {
        viewModelScope.launch {
            val phraseList = translateRepository.getPhrases()
            _uiState.update { it.copy(phrases = phraseList) }
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(input = text) }
    }

    fun clearInput() {
        _uiState.update {
            it.copy(
                input = "",
                translationResult = null,
                errorMessage = null,
                permissionError = null,
                isProcessing = false,
                isRecording = false
            )
        }
    }

    fun setPermissionError(message: String?) {
        _uiState.update { it.copy(permissionError = message) }
    }

    fun setRecordingState(recording: Boolean) {
        _uiState.update { it.copy(isRecording = recording) }
    }

    fun performOcr(imageBase64: String, mimeType: String = "image/jpeg") {
        _uiState.update {
            it.copy(
                isProcessing = true,
                errorMessage = null,
                permissionError = null,
                translationResult = null
            )
        }

        viewModelScope.launch {
            val result = translateRepository.translateOcr(imageBase64, mimeType)
            result.fold(
                onSuccess = { ocrText ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            input = ocrText
                        )
                    }
                    translate()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "ocr_failed"
                        )
                    }
                }
            )
        }
    }

    fun performStt(audioBase64: String, mimeType: String = "audio/mpeg", language: String = "zh") {
        _uiState.update {
            it.copy(
                isProcessing = true,
                errorMessage = null,
                permissionError = null,
                translationResult = null
            )
        }

        viewModelScope.launch {
            val result = translateRepository.translateStt(audioBase64, mimeType, language)
            result.fold(
                onSuccess = { sttText ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            input = sttText
                        )
                    }
                    translate()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "stt_failed"
                        )
                    }
                }
            )
        }
    }

    fun swapLanguages() {
        _uiState.update {
            it.copy(
                fromLanguage = it.toLanguage,
                toLanguage = it.fromLanguage,
                // Automatically clear prior results to keep UI fresh
                translationResult = null,
                errorMessage = null,
                permissionError = null
            )
        }
    }

    fun setFromLanguage(code: String) {
        _uiState.update {
            it.copy(
                fromLanguage = code,
                translationResult = null,
                errorMessage = null,
            )
        }
    }

    fun setToLanguage(code: String) {
        _uiState.update {
            it.copy(
                toLanguage = code,
                translationResult = null,
                errorMessage = null,
            )
        }
    }

    fun speak(text: String, languageCode: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val ttsLanguageName = SupportedLanguages.byCode(languageCode).ttsLanguageName
            translateRepository.translateTts(text, ttsLanguageName).fold(
                onSuccess = { audioUrl ->
                    _uiState.update {
                        it.copy(ttsAudioUrl = audioUrl, ttsFallbackText = null, ttsFallbackLanguageCode = null)
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(ttsFallbackText = text, ttsFallbackLanguageCode = languageCode, ttsAudioUrl = null)
                    }
                },
            )
        }
    }

    fun clearTtsAudioUrl() {
        _uiState.update { it.copy(ttsAudioUrl = null) }
    }

    fun clearTtsFallback() {
        _uiState.update { it.copy(ttsFallbackText = null, ttsFallbackLanguageCode = null) }
    }

    fun translate() {
        val state = _uiState.value
        val textToTranslate = state.input.trim()
        if (textToTranslate.isBlank()) return

        _uiState.update {
            it.copy(
                translating = true,
                translationResult = null,
                errorMessage = null,
                permissionError = null
            )
        }

        viewModelScope.launch {
            val result = translateRepository.translateText(textToTranslate, state.fromLanguage, state.toLanguage)
            result.fold(
                onSuccess = { translation ->
                    _uiState.update {
                        it.copy(
                            translating = false,
                            translationResult = translation
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            translating = false,
                            errorMessage = error.message ?: "Unknown error"
                        )
                    }
                }
            )
        }
    }
}
