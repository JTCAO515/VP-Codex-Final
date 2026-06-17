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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.data.repository.TripAutoSave
import space.jtcao.visepanda.data.trip.toTripAsset
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.repository.ChatRepository
import space.jtcao.visepanda.domain.usecase.StreamChatUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ChatTripSaveTest {

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
    fun `completed itinerary response should auto save trip asset`() = runTest {
        val savedTrips = mutableListOf<TripAsset>()
        val repo = object : ChatRepository {
            override fun stream(
                cityId: String?,
                history: List<ChatMessageItem>,
                prompt: String
            ) = flow {
                emit(ChatSseEvent.Token("### Trip Overview\n"))
                emit(ChatSseEvent.Token("**Day 1: The Bund**\n"))
                emit(ChatSseEvent.Token("**Day 2: French Concession**"))
                emit(ChatSseEvent.Done)
            }
        }

        val viewModel = ChatViewModel(
            streamChat = StreamChatUseCase(repo),
            saveTripAsset = { savedTrips += it },
            createTripAssetCandidate = { cityId, content ->
                TripAutoSave.createTripOrNull(cityId, content)?.toTripAsset()
            }
        )

        viewModel.send(prompt = "Plan Shanghai", cityId = "shanghai")
        advanceUntilIdle()

        assertEquals(1, savedTrips.size)
        assertEquals("shanghai", savedTrips.single().cityId)
        assertTrue(savedTrips.single().content.contains("Day 2"))
    }
}
