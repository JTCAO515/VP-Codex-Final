package space.jtcao.visepanda.data.model

/**
 * Server-Sent Events from the VisePanda chat API.
 *
 * The SSE stream emits these event types:
 *   event: token  → data: "some text"
 *   event: split  → data: {"boundary": true}
 *   event: image  → data: {"key": "..", "url": "..", "label": ".."}
 *   event: faq    → data: {"id": "..", "title": "..", "icon": ".."}
 *   event: done   → data: ""
 *   event: error  → data: {"message": ".."}
 */
sealed class ChatEvent {
    /** A text token to append to the current message */
    data class Token(val text: String) : ChatEvent()

    /** Section boundary — used to split long messages into visual segments */
    data class Split(val boundary: Boolean = true) : ChatEvent()

    /** An inline image card */
    data class Image(
        val key: String,
        val url: String,
        val label: String
    ) : ChatEvent()

    /** A FAQ chip the user can tap to send as a new message */
    data class Faq(
        val id: String,
        val title: String,
        val icon: String
    ) : ChatEvent()

    /** Stream complete */
    data object Done : ChatEvent()

    /** An error occurred during streaming */
    data class Error(val message: String) : ChatEvent()
}

/**
 * A chat message in the conversation (for both request and display).
 */
data class ChatMessage(
    val role: String,    // "user" | "assistant"
    val content: String,
    val images: List<ChatImage> = emptyList(),
    val faqs: List<ChatFaq> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatImage(
    val key: String,
    val url: String,
    val label: String
)

data class ChatFaq(
    val id: String,
    val title: String,
    val icon: String
)
