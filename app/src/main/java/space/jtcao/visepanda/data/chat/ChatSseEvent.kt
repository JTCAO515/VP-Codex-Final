package space.jtcao.visepanda.data.chat

sealed interface ChatSseEvent {
    data class Token(val text: String) : ChatSseEvent
    data object Split : ChatSseEvent
    data class Image(val key: String, val url: String, val label: String) : ChatSseEvent
    data class Faq(val id: String, val title: String, val icon: String) : ChatSseEvent
    data object Done : ChatSseEvent
    data class Error(val message: String) : ChatSseEvent
}
