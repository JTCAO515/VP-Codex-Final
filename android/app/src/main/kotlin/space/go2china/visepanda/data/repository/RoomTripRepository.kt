package space.go2china.visepanda.data.repository

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import space.go2china.visepanda.data.local.TripCacheDao
import space.go2china.visepanda.data.local.TripCacheEntity
import space.go2china.visepanda.data.model.ButlerAlert
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.ButlerMessageRole
import space.go2china.visepanda.data.model.ButlerTurnResult
import space.go2china.visepanda.data.model.CanvasPatchApplier
import space.go2china.visepanda.data.model.MockTripData
import space.go2china.visepanda.data.model.TripState
import space.go2china.visepanda.data.remote.ButlerApiService
import space.go2china.visepanda.data.remote.ButlerChatRequest
import space.go2china.visepanda.data.remote.RemoteChatMessage
import space.go2china.visepanda.data.serialization.TripJson

@Singleton
class RoomTripRepository @Inject constructor(
    private val tripCacheDao: TripCacheDao,
    private val butlerApiService: ButlerApiService,
) : TripRepository {

    private val offline = MutableStateFlow(false)

    override fun observeActiveTrip(): Flow<TripState?> =
        tripCacheDao.observe(ACTIVE_TRIP_ID).map { entity ->
            entity?.decodeTripOrNull() ?: MockTripData.initialTripState
        }

    override fun observeOffline(): Flow<Boolean> = offline

    override fun observeButlerMessages(): Flow<List<ButlerChatMessage>> =
        tripCacheDao.observe(ACTIVE_TRIP_ID).map { entity ->
            entity?.decodeMessagesOrNull().orEmpty()
        }

    override suspend fun sendButlerMessage(message: String): ButlerTurnResult = withContext(Dispatchers.IO) {
        val trimmed = message.trim()
        require(trimmed.isNotEmpty()) { "Message cannot be blank." }

        val beforeEntity = tripCacheDao.get(ACTIVE_TRIP_ID)
        val beforeTrip = beforeEntity?.decodeTripOrNull() ?: MockTripData.initialTripState
        val beforeMessages = beforeEntity?.decodeMessagesOrNull().orEmpty()
        val now = System.currentTimeMillis()
        val userMessage = ButlerChatMessage(
            id = "native-user-$now",
            role = ButlerMessageRole.User,
            content = trimmed,
            createdAtEpochMillis = now,
        )
        val messagesWithUser = beforeMessages + userMessage

        val remoteResult = runCatching {
            butlerApiService.chat(
                ButlerChatRequest(
                    message = trimmed,
                    trip = beforeTrip,
                    messages = messagesWithUser.map { it.toRemoteMessage() },
                ),
            )
        }

        val response = remoteResult.getOrNull()
        val patch = response?.patch ?: NativeButlerFallback.createPatch(trimmed, beforeTrip)
        val nextTrip = CanvasPatchApplier.apply(beforeTrip, patch)
        val assistantMessage = ButlerChatMessage(
            id = "native-assistant-$now",
            role = ButlerMessageRole.Assistant,
            content = patch.assistantMessage,
            response = patch.assistantResponse,
            createdAtEpochMillis = now + 1,
        )
        val offlineFallback = response == null
        offline.value = offlineFallback

        persist(nextTrip, messagesWithUser + assistantMessage)

        ButlerTurnResult(
            assistantMessage = assistantMessage,
            trip = nextTrip,
            suggestions = response?.suggestions?.ifEmpty { NativeButlerFallback.suggestions() }
                ?: NativeButlerFallback.suggestions(),
            modelLabel = response?.modelLabel ?: "native mock fallback",
            offlineFallback = offlineFallback,
        )
    }

    override suspend fun renameActiveTrip(newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        updateTrip { trip ->
            trip.copy(summary = trip.summary.copy(title = trimmed))
        }
    }

    override suspend fun setAlertDone(alert: ButlerAlert, done: Boolean) {
        updateTrip { trip ->
            trip.copy(
                alerts = trip.alerts.map { current ->
                    if (current == alert) current.copy(done = done) else current
                },
            )
        }
    }

    private suspend fun updateTrip(transform: (TripState) -> TripState) = withContext(Dispatchers.IO) {
        val entity = tripCacheDao.get(ACTIVE_TRIP_ID)
        val currentTrip = entity?.decodeTripOrNull() ?: MockTripData.initialTripState
        val currentMessages = entity?.decodeMessagesOrNull().orEmpty()
        persist(transform(currentTrip), currentMessages)
    }

    private suspend fun persist(trip: TripState, messages: List<ButlerChatMessage>) {
        tripCacheDao.upsert(
            TripCacheEntity(
                id = ACTIVE_TRIP_ID,
                tripStateJson = TripJson.encodeTrip(trip),
                messagesJson = TripJson.encodeMessages(messages),
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    private fun ButlerChatMessage.toRemoteMessage(): RemoteChatMessage =
        RemoteChatMessage(
            id = id,
            role = when (role) {
                ButlerMessageRole.User -> "user"
                ButlerMessageRole.Assistant -> "assistant"
            },
            content = content,
            createdAt = Instant.ofEpochMilli(createdAtEpochMillis).toString(),
        )

    private fun TripCacheEntity.decodeTripOrNull(): TripState? =
        runCatching { TripJson.decodeTrip(tripStateJson) }.getOrNull()

    private fun TripCacheEntity.decodeMessagesOrNull(): List<ButlerChatMessage>? =
        runCatching { TripJson.decodeMessages(messagesJson) }.getOrNull()

    private companion object {
        const val ACTIVE_TRIP_ID = "active"
    }
}
