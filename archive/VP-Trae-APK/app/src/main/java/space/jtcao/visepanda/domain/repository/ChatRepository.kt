package space.jtcao.visepanda.domain.repository

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.domain.model.ChatMessageItem

interface ChatRepository {
    fun stream(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent>
}
