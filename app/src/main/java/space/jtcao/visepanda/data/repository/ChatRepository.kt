package space.jtcao.visepanda.data.repository

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.data.api.SseClient
import space.jtcao.visepanda.data.model.ChatEvent
import space.jtcao.visepanda.data.model.ChatMessage

/**
 * Repository for the AI chat feature.
 *
 * Provides both streaming (SSE) and synchronous chat options.
 * The streaming path is the primary mode — delivers tokens
 * in real-time via Kotlin Flow.
 */
class ChatRepository(
    private val sseClient: SseClient = SseClient()
) {

    /**
     * Stream a chat response — emits [ChatEvent] tokens/images/FAQs
     * in real-time via SSE. Use this for the live chat UI.
     */
    fun streamChat(
        messages: List<ChatMessage>,
        city: String? = null
    ): Flow<ChatEvent> {
        return sseClient.streamChat(messages, city)
    }

    /**
     * Non-streaming fallback — blocks until the full response is received.
     * Use this for simpler scenarios like regenerating the last message.
     */
    suspend fun chatSync(
        messages: List<ChatMessage>,
        city: String? = null
    ): String {
        return sseClient.chatSync(messages, city)
    }
}
