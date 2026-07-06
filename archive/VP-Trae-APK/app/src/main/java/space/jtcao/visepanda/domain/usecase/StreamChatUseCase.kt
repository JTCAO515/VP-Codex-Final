package space.jtcao.visepanda.domain.usecase

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.repository.ChatRepository

class StreamChatUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke(
        cityId: String?,
        history: List<ChatMessageItem>,
        prompt: String
    ): Flow<ChatSseEvent> = repository.stream(cityId, history, prompt)
}
