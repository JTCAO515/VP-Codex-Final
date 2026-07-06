package space.jtcao.visepanda.feature.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.repository.ChatRepository
import space.jtcao.visepanda.domain.usecase.StreamChatUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `send should append streamed assistant text`() = runTest {
        val repo = object : ChatRepository {
            override fun stream(
                cityId: String?,
                history: List<ChatMessageItem>,
                prompt: String
            ) = flow {
                emit(ChatSseEvent.Token("Hello"))
                emit(ChatSseEvent.Token(" China"))
                emit(ChatSseEvent.Done)
            }
        }

        val viewModel = ChatViewModel(StreamChatUseCase(repo))

        viewModel.send("Plan Shanghai", "shanghai")
        advanceUntilIdle()

        assertTrue(
            viewModel.uiState.value.messages.any {
                it.role == "assistant" && it.content.contains("Hello China")
            }
        )
    }
}
