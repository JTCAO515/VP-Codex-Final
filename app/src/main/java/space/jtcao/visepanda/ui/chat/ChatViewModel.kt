package space.jtcao.visepanda.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.model.ChatEvent
import space.jtcao.visepanda.data.model.ChatFaq
import space.jtcao.visepanda.data.model.ChatImage
import space.jtcao.visepanda.data.model.ChatMessage
import space.jtcao.visepanda.data.repository.ChatRepository
import space.jtcao.visepanda.data.repository.TripRepository

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
 * Manages the conversation state:
 * - Sends user messages
 * - Collects SSE stream events (Token/Split/Image/Faq/Done)
 * - Manages streaming vs. idle state
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val tripRepo = TripRepository(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null
    private var accumulatedText = ""
    private var accumulatedImages = mutableListOf<ChatImage>()
    private var accumulatedFaqs = mutableListOf<ChatFaq>()

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
                // Flush accumulated text as a completed message segment
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
                autoSaveTrip()
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
        }
    }

    private fun finalizeMessage() {
        flushAccumulated(isSplit = false)
        streamJob = null
        _uiState.value = _uiState.value.copy(isStreaming = false)
    }

    /** Auto-save trip when the assistant response looks like an itinerary. */
    private fun autoSaveTrip() {
        val lastMessages = _uiState.value.messages
        if (lastMessages.isEmpty()) return
        val lastAssistant = lastMessages.lastOrNull { it.role == "assistant" } ?: return
        val text = lastAssistant.content
        // Heuristic: check for day-by-day itinerary pattern
        if (!text.contains(Regex("(?i)day\s*\d+")) && !text.contains("行程") && !text.contains("路线")) return
        // Launch save in background
        kotlinx.coroutines.MainScope().launch {
            try {
                tripRepo.saveTrip(
                    name = text.take(50).replace("
", " ").trim(),
                    itinerary = text
                )
            } catch (_: Exception) { /* Best-effort save */ }
        }
    }
}
