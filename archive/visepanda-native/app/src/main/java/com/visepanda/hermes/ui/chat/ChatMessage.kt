package com.visepanda.hermes.ui.chat

data class ChatMessage(
    val id: String,
    val content: String = "",
    val isUser: Boolean = false,
    val type: MessageType = MessageType.TEXT
) {
    enum class MessageType {
        TEXT,
        SPLIT,
        IMAGE,
        FAQ,
        LOADING
    }
}

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
