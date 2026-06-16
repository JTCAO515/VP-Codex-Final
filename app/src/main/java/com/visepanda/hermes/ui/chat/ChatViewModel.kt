package com.visepanda.hermes.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visepanda.network.sse.SseClient
import com.visepanda.network.sse.SseEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val sseClient = SseClient()
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var currentAiMessageId: String? = null
    private val suggestionPool = listOf(
        "Plan a 3-day Beijing trip",
        "Best time to visit China",
        "Local cuisine recommendations",
        "Top attractions in Shanghai",
        "How to get a visa for China",
        "Is VPN needed in China?",
        "Budget for 7 days in China",
        "Must-try foods in Chengdu"
    )
    val suggestions: List<String> = suggestionPool

    fun sendMessage(text: String) {
        if (text.isBlank() || _state.value.isLoading) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            isUser = true,
            type = ChatMessage.MessageType.TEXT
        )

        val loadingMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = false,
            type = ChatMessage.MessageType.LOADING
        )

        currentAiMessageId = loadingMsg.id

        _state.update {
            it.copy(
                messages = it.messages + listOf(userMsg, loadingMsg),
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                // Correct API format: { "messages": [{ "role": "user", "content": "..." }] }
                val escaped = text
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                val requestBody = """{"messages":[{"role":"user","content":"$escaped"}]}"""

                sseClient.connect(
                    url = "https://www.go2china.space/api/chat",
                    requestBody = requestBody
                ).collect { event ->
                    when (event) {
                        is SseEvent.Token -> appendToCurrentAi(event.text)
                        is SseEvent.Split -> appendToCurrentAi("\n\n---\n\n" + event.text)
                        is SseEvent.Image -> appendToCurrentAi("\n[Image: ${event.url}]\n")
                        is SseEvent.Faq -> appendToCurrentAi("\n[FAQ: ${event.data}]\n")
                        is SseEvent.Done -> finalizeAiMessage()
                        is SseEvent.Error -> {
                            appendToCurrentAi("Sorry, I couldn't reach the server. Let me help with what I know.")
                            finalizeAiMessage()
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback mock response if SSE fails
                mockStreamResponse(text)
            }
        }
    }

    private fun appendToCurrentAi(text: String) {
        val msgId = currentAiMessageId ?: return
        _state.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == msgId) {
                        msg.copy(content = msg.content + text, type = ChatMessage.MessageType.TEXT)
                    } else msg
                }
            )
        }
    }

    private fun finalizeAiMessage() {
        _state.update { it.copy(isLoading = false) }
        currentAiMessageId = null
    }

    private fun mockStreamResponse(userText: String) {
        val responses = mapOf(
            "Plan a 3-day Beijing trip" to "Here's a suggested 3-day Beijing itinerary:\n\n**Day 1:** Tiananmen Square -> Forbidden City -> Jingshan Park\n**Day 2:** Great Wall at Mutianyu (half-day trip)\n**Day 3:** Temple of Heaven -> Summer Palace -> Wangfujing Night Market\n\nWould you like me to add estimated costs or recommended hotels for each day?",
            "Best time to visit China" to "The best time to visit China depends on your destinations:\n\nSpring (Mar-May): Mild temperatures, cherry blossoms. Best for Beijing, Shanghai, Xi'an.\nAutumn (Sep-Oct): Pleasant weather, golden leaves. Peak season for Guilin and Zhangjiajie.\nSummer (Jun-Aug): Hot and rainy in most cities. High season, but ideal for Tibet and Inner Mongolia.\nWinter (Nov-Feb): Cold but fewer crowds. Great for Harbin Ice Festival.\n\nRecommendation: April-May or September-October for the best overall experience."
        )

        viewModelScope.launch {
            val reply = responses[userText] ?: "Great question! I can help you plan a trip to China. Could you tell me which cities you're interested in and how many days you're planning to stay?"
            reply.forEach { char ->
                appendToCurrentAi(char.toString())
                kotlinx.coroutines.delay(20)
            }
            finalizeAiMessage()
        }
    }
}
