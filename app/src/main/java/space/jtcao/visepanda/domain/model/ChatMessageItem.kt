package space.jtcao.visepanda.domain.model

data class ChatMessageItem(
    val id: String,
    val role: String,
    val content: String,
    val cityId: String? = null
)
