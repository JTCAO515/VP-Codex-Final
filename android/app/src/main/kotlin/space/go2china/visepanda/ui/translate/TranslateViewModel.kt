package space.go2china.visepanda.ui.translate

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.repository.TranslateRepository
import java.util.Locale
import javax.inject.Inject

sealed interface TranslateUiState {
    data object Idle : TranslateUiState
    data object Loading : TranslateUiState
    val currentTranslatedText: String?
        get() = when (this) {
            is Success -> translatedText
            else -> null
        }
    data class Success(val translatedText: String) : TranslateUiState
    data class Error(val message: String) : TranslateUiState
}

@OptIn(FlowPreview::class)
@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val repository: TranslateRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isEnToZh = MutableStateFlow(true)
    val isEnToZh: StateFlow<Boolean> = _isEnToZh.asStateFlow()

    private val _uiState = MutableStateFlow<TranslateUiState>(TranslateUiState.Idle)
    val uiState: StateFlow<TranslateUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINESE)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    _isTtsReady.value = true
                }
            }
        }

        _inputText
            .debounce(800)
            .filter { it.isNotBlank() }
            .onEach { text ->
                performTranslation(text)
            }
            .launchIn(viewModelScope)
    }

    fun setInputText(text: String) {
        _inputText.value = text
        if (text.isBlank()) {
            _uiState.value = TranslateUiState.Idle
        }
    }

    fun toggleLanguageDirection() {
        _isEnToZh.value = !_isEnToZh.value
        _inputText.value = ""
        _uiState.value = TranslateUiState.Idle
    }

    fun triggerTranslate() {
        val text = _inputText.value
        if (text.isNotBlank()) {
            performTranslation(text)
        }
    }

    private fun performTranslation(text: String) {
        viewModelScope.launch {
            _uiState.value = TranslateUiState.Loading
            val from = if (_isEnToZh.value) "en" else "zh"
            val to = if (_isEnToZh.value) "zh" else "en"
            
            repository.translate(text, from, to)
                .onSuccess { translated ->
                    _uiState.value = TranslateUiState.Success(translated)
                }
                .onFailure { exception ->
                    val errorMsg = exception.message ?: "Translation connection failed"
                    _uiState.value = TranslateUiState.Error(errorMsg)
                }
        }
    }

    fun speakChinese(text: String) {
        if (_isTtsReady.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
