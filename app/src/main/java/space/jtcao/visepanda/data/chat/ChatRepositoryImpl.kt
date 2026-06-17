package space.jtcao.visepanda.data.chat

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val remote: ChatRemoteDataSource
) : ChatRepository {
    override fun stream(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent> = remote.stream(cityId, history, prompt)
}
