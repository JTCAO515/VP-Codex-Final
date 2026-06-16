package space.jtcao.visepanda.ui.chat

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import space.jtcao.visepanda.data.model.ChatEvent
import space.jtcao.visepanda.data.model.ChatFaq
import space.jtcao.visepanda.data.model.ChatImage
import space.jtcao.visepanda.data.model.ChatMessage
import space.jtcao.visepanda.data.repository.ChatRepository

private val Application.chatDataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_history")

/**
 * UI state for the Chat screen.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val currentStreamText: String = "",
    val currentImages: List<ChatImage> = emptyList(),
    val currentFaqs: List<ChatFaq> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for the Chat screen.
 *
 * - Manages SSE streaming conversation
 * - Persists chat history via DataStore (survives rotation & process death)
 * - Restores messages on init
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val json = Json { ignoreUnknownKeys = true }
    private val dataStore = application.chatDataStore

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null
    private var accumulatedText = ""
    private var accumulatedImages = mutableListOf<ChatImage>()
    private var accumulatedFaqs = mutableListOf<ChatFaq>()

    init {
        loadHistory()
    }

    /** Restore chat history from DataStore */
    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val saved = dataStore.data.map { prefs ->
                    prefs[CHAT_HISTORY_KEY] ?: ""
                }.first()
                if (saved.isNotBlank()) {
                    val messages: List<ChatMessage> = json.decodeFromString(saved)
                    _uiState.value = _uiState.value.copy(messages = messages)
                }
            } catch (_: Exception) {
                // Corrupted history — start fresh
            }
        }
    }

    /** Persist current messages to DataStore */
    private fun saveHistory() {
        viewModelScope.launch {
            try {
                val encoded = json.encodeToString(_uiState.value.messages)
                dataStore.edit { prefs -> prefs[CHAT_HISTORY_KEY] = encoded }
            } catch (_: Exception) {
                // Best-effort persistence
            }
        }
    }

    /** Send a user message and start streaming the AI response */
    fun sendMessage(text: String, city: String? = null) {
        if (text.isBlank() || _uiState.value.isStreaming) return

        // Add user message
        val userMsg = ChatMessage(
            role = "user",
            content = text,
            timestamp = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg,
            error = null
        )
        saveHistory()

        // Start streaming AI response
        val allMessages = _uiState.value.messages + userMsg
        accumulatedText = ""
        accumulatedImages.clear()
        accumulatedFaqs.clear()

        _uiState.value = _uiState.value.copy(isStreaming = true)

        streamJob = viewModelScope.launch {
            repository.streamChat(allMessages, city)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isStreaming = false,
                        error = e.message ?: "Stream error"
                    )
                }
                .collect { event ->
                    handleEvent(event)
                }
        }
    }

    /** Stop the current streaming response */
    fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        finalizeMessage()
    }

    /** Clear the entire conversation */
    fun clearChat() {
        streamJob?.cancel()
        _uiState.value = ChatUiState()
        accumulatedText = ""
        accumulatedImages.clear()
        accumulatedFaqs.clear()
        saveHistory()
    }

    // ── Private ──

    private fun handleEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.Token -> {
                accumulatedText += event.text
                _uiState.value = _uiState.value.copy(
                    currentStreamText = accumulatedText,
                    currentImages = accumulatedImages.toList(),
                    currentFaqs = accumulatedFaqs.toList()
                )
            }
            is ChatEvent.Split -> {
                flushAccumulated(isSplit = true)
            }
            is ChatEvent.Image -> {
                accumulatedImages.add(
                    ChatImage(key = event.key, url = event.url, label = event.label)
                )
                _uiState.value = _uiState.value.copy(
                    currentImages = accumulatedImages.toList()
                )
            }
            is ChatEvent.Faq -> {
                accumulatedFaqs.add(
                    ChatFaq(id = event.id, title = event.title, icon = event.icon)
                )
                _uiState.value = _uiState.value.copy(
                    currentFaqs = accumulatedFaqs.toList()
                )
            }
            is ChatEvent.Done -> {
                flushAccumulated(isSplit = false)
                streamJob = null
                _uiState.value = _uiState.value.copy(isStreaming = false)
            }
            is ChatEvent.Error -> {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    error = event.message
                )
            }
        }
    }

    private fun flushAccumulated(isSplit: Boolean) {
        if (accumulatedText.isNotEmpty() || accumulatedImages.isNotEmpty()) {
            val msg = ChatMessage(
                role = "assistant",
                content = accumulatedText,
                images = accumulatedImages.toList(),
                faqs = accumulatedFaqs.toList()
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + msg,
                currentStreamText = "",
                currentImages = emptyList(),
                currentFaqs = emptyList()
            )
            accumulatedText = ""
            accumulatedImages.clear()
            accumulatedFaqs.clear()
            saveHistory()
        }
    }

    private fun finalizeMessage() {
        flushAccumulated(isSplit = false)
        streamJob = null
        _uiState.value = _uiState.value.copy(isStreaming = false)
    }

    companion object {
        private val CHAT_HISTORY_KEY = stringPreferencesKey("chat_messages")
    }
}
