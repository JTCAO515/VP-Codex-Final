package space.jtcao.visepanda.data.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.jtcao.visepanda.data.model.ChatEvent as LegacyChatEvent
import space.jtcao.visepanda.data.model.ChatMessage as LegacyChatMessage
import space.jtcao.visepanda.data.repository.ChatRepository as LegacyChatRepository
import space.jtcao.visepanda.domain.model.ChatMessageItem

interface ChatRemoteDataSource {
    fun stream(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent>
}

class ChatRemoteDataSourceImpl(
    private val legacyRepository: LegacyChatRepository = LegacyChatRepository()
) : ChatRemoteDataSource {

    override fun stream(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent> {
        return legacyRepository.streamChat(
            messages = history.toLegacyMessages(prompt),
            city = cityId
        ).map(LegacyChatEvent::toChatSseEvent)
    }
}

private fun List<ChatMessageItem>.toLegacyMessages(prompt: String): List<LegacyChatMessage> {
    val mappedHistory = map(ChatMessageItem::toLegacyMessage)
    val promptAlreadyPresent = lastOrNull()?.role == "user" && lastOrNull()?.content == prompt

    return if (prompt.isBlank() || promptAlreadyPresent) {
        mappedHistory
    } else {
        mappedHistory + LegacyChatMessage(
            role = "user",
            content = prompt
        )
    }
}

private fun ChatMessageItem.toLegacyMessage(): LegacyChatMessage =
    LegacyChatMessage(
        role = role,
        content = content
    )

private fun LegacyChatEvent.toChatSseEvent(): ChatSseEvent =
    when (this) {
        is LegacyChatEvent.Token -> ChatSseEvent.Token(text)
        is LegacyChatEvent.Split -> ChatSseEvent.Split
        is LegacyChatEvent.Image -> ChatSseEvent.Image(
            key = key,
            url = url,
            label = label
        )
        is LegacyChatEvent.Faq -> ChatSseEvent.Faq(
            id = id,
            title = title,
            icon = icon
        )
        is LegacyChatEvent.Done -> ChatSseEvent.Done
        is LegacyChatEvent.Error -> ChatSseEvent.Error(message)
    }
