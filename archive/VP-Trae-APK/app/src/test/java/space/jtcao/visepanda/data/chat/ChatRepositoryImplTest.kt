package space.jtcao.visepanda.data.chat

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import space.jtcao.visepanda.domain.model.ChatMessageItem

class ChatRepositoryImplTest {

    @Test
    fun `stream should delegate to remote data source`() = runBlocking {
        val repo = ChatRepositoryImpl(
            remote = object : ChatRemoteDataSource {
                override fun stream(
                    cityId: String?,
                    history: List<ChatMessageItem>,
                    prompt: String
                ) = flowOf(ChatSseEvent.Token("delegated"))
            }
        )

        val event = repo.stream(null, emptyList(), "Hello").first()

        assertTrue(event is ChatSseEvent.Token)
    }
}
