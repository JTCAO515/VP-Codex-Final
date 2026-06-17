package space.jtcao.visepanda.feature.chat

import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.model.ChatSuggestion

private val defaultSuggestions = listOf(
    ChatSuggestion(
        id = "first-trip",
        title = "First trip to China",
        prompt = "Plan a 5-day first trip to China with Beijing and Shanghai."
    ),
    ChatSuggestion(
        id = "food-route",
        title = "Food route",
        prompt = "Design a food-focused route covering Chengdu and Shanghai."
    )
)

data class ChatUiState(
    val cityId: String? = null,
    val input: String = "",
    val isStreaming: Boolean = false,
    val messages: List<ChatMessageItem> = emptyList(),
    val suggestions: List<ChatSuggestion> = defaultSuggestions,
    val streamingText: String = "",
    val streamingImages: List<ChatSseEvent.Image> = emptyList(),
    val streamingFaqs: List<ChatSseEvent.Faq> = emptyList(),
    val error: String? = null
)
