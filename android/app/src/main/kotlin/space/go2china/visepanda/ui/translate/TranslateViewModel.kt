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
import space.go2china.visepanda.data.model.TranslateResult
import space.go2china.visepanda.data.repository.TranslateRepository
import javax.inject.Inject

data class TranslateUiState(
    val input: String = "",
    val translateToChinese: Boolean = true, // true: en -> zh, false: zh -> en
    val translating: Boolean = false,
    val translationResult: TranslateResult? = null,
    val errorMessage: String? = null,
    val phrases: List<Phrase> = emptyList(),
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
                errorMessage = null
            )
        }
    }

    fun toggleDirection() {
        _uiState.update {
            it.copy(
                translateToChinese = !it.translateToChinese,
                // Automatically clear prior results to keep UI fresh
                translationResult = null,
                errorMessage = null
            )
        }
    }

    fun translate() {
        val state = _uiState.value
        val textToTranslate = state.input.trim()
        if (textToTranslate.isBlank()) return

        _uiState.update {
            it.copy(
                translating = true,
                translationResult = null,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val from = if (state.translateToChinese) "en" else "zh"
            val to = if (state.translateToChinese) "zh" else "en"
            
            val result = translateRepository.translateText(textToTranslate, from, to)
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
