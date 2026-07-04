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
    private val gson = com.google.gson.Gson()

    private var pendingPoiMessage: String? = null
    private var pendingPoiPayload: String? = null

    override fun setPendingExplorePoi(message: String, payload: String) {
        pendingPoiMessage = message
        pendingPoiPayload = payload
    }

    override fun getPendingExplorePoiMessage(): String? = pendingPoiMessage

    override fun clearPendingExplorePoi() {
        pendingPoiMessage = null
        pendingPoiPayload = null
    }

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

        val activePoiPayload = pendingPoiPayload
        clearPendingExplorePoi()

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
                    poi = activePoiPayload
                ),
            )
        }

        val response = remoteResult.getOrNull()
        val rawPatch = response?.patch ?: NativeButlerFallback.createPatch(trimmed, beforeTrip)
        
        val payloadObj = activePoiPayload?.let {
            runCatching { gson.fromJson(it, space.go2china.visepanda.data.explore.ExploreAddToTripPayload::class.java) }.getOrNull()
        }
        val patch = applyExplorePoiToPatch(rawPatch, beforeTrip, payloadObj)

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

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace(Regex("[（(].*?[）)]"), "")
            .replace(Regex("[^a-z0-9\\u4e00-\\u9fff]+"), " ")
            .trim()
    }

    private fun matchesPayload(block: space.go2china.visepanda.data.model.TripBlock, payload: space.go2china.visepanda.data.explore.ExploreAddToTripPayload): Boolean {
        val blockTitle = normalize(block.title)
        val payloadName = normalize(payload.name)
        if (blockTitle.isEmpty() || payloadName.isEmpty()) return false
        return blockTitle.contains(payloadName) || payloadName.contains(blockTitle)
    }

    private fun enrichBlock(block: space.go2china.visepanda.data.model.TripBlock, payload: space.go2china.visepanda.data.explore.ExploreAddToTripPayload): space.go2china.visepanda.data.model.TripBlock {
        return block.copy(
            address = block.address ?: payload.address,
            phone = block.phone ?: payload.phone,
            openingHours = block.openingHours ?: payload.openingHours,
            mapUrl = block.mapUrl ?: payload.mapUrl,
            sourceLabel = block.sourceLabel ?: payload.sourceLabel,
            coordinates = block.coordinates ?: payload.coordinates?.let { space.go2china.visepanda.data.model.TripLatLng(it.lat, it.lng) },
            bookingCandidates = block.bookingCandidates.ifEmpty { payload.bookingCandidates.orEmpty() }
        )
    }

    private fun createExploreBlock(payload: space.go2china.visepanda.data.explore.ExploreAddToTripPayload): space.go2china.visepanda.data.model.TripBlock {
        val contextDetail = if (!payload.context.isNullOrBlank()) " (${payload.context})" else ""
        return space.go2china.visepanda.data.model.TripBlock(
            time = "Flexible",
            title = payload.name,
            description = "Added from Explore as a ${payload.category}${contextDetail} candidate for ${payload.cityName}. VisePanda can rebalance it into a specific time block.",
            address = payload.address,
            phone = payload.phone,
            openingHours = payload.openingHours,
            mapUrl = payload.mapUrl,
            sourceLabel = payload.sourceLabel,
            coordinates = payload.coordinates?.let { space.go2china.visepanda.data.model.TripLatLng(it.lat, it.lng) },
            bookingCandidates = payload.bookingCandidates.orEmpty()
        )
    }

    private fun applyExplorePoiToPatch(
        patch: space.go2china.visepanda.data.model.CanvasPatch,
        currentTrip: space.go2china.visepanda.data.model.TripState,
        payload: space.go2china.visepanda.data.explore.ExploreAddToTripPayload?
    ): space.go2china.visepanda.data.model.CanvasPatch {
        if (payload == null) return patch

        val sourceDays = if (!patch.days.isNullOrEmpty()) patch.days else currentTrip.days
        if (sourceDays.isEmpty()) return patch

        var foundMatch = false
        val days = sourceDays.map { day ->
            day.copy(
                blocks = day.blocks.map { block ->
                    if (matchesPayload(block, payload)) {
                        foundMatch = true
                        enrichBlock(block, payload)
                    } else {
                        block
                    }
                }
            )
        }

        val updatedDays = if (!foundMatch) {
            val targetIndex = days.indexOfFirst { normalize(it.city) == normalize(payload.cityName) }
            val resolvedIndex = if (targetIndex >= 0) targetIndex else 0
            days.mapIndexed { idx, day ->
                if (idx == resolvedIndex) {
                    day.copy(
                        status = "revised",
                        blocks = day.blocks + createExploreBlock(payload)
                    )
                } else {
                    day
                }
            }
        } else {
            days
        }

        return patch.copy(
            days = updatedDays,
            reason = if (foundMatch) {
                "${patch.reason} Explore POI details were attached to the matching trip block."
            } else {
                "${patch.reason} Explore POI was added as a flexible candidate block."
            }
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
