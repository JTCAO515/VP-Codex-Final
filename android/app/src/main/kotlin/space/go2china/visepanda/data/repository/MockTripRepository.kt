package space.go2china.visepanda.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import space.go2china.visepanda.data.model.ButlerAlert
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.ButlerMessageRole
import space.go2china.visepanda.data.model.ButlerTurnResult
import space.go2china.visepanda.data.model.MockTripData
import space.go2china.visepanda.data.model.TripState

/**
 * v0.3.3/v0.3.4 implementation: an in-memory copy of the same mock trip the
 * web app's mock Butler seeds ([MockTripData]). Deliberately not backed by
 * Room yet — the v0.3.3 scope is "define the repository interfaces and
 * offline-first shape", not "wire real persistence", to keep the first
 * native round narrow enough to actually finish (see
 * docs/planning/v0.3.2-android-planning-synthesis.md, "v0.3.3 — Android
 * Native Foundation" scope).
 *
 * Always reports [observeOffline] as false: there is no network layer yet
 * for this to be honest about, so claiming "offline" here would be a fake
 * state, not a real one.
 */
@Singleton
class MockTripRepository @Inject constructor() : TripRepository {

    private val activeTrip: MutableStateFlow<TripState?> =
        MutableStateFlow(MockTripData.initialTripState)

    private val offline: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val messages: MutableStateFlow<List<ButlerChatMessage>> = MutableStateFlow(emptyList())

    override fun observeActiveTrip(): StateFlow<TripState?> = activeTrip

    override fun observeOffline(): StateFlow<Boolean> = offline

    override fun observeButlerMessages(): StateFlow<List<ButlerChatMessage>> = messages

    override suspend fun sendButlerMessage(message: String): ButlerTurnResult {
        val now = System.currentTimeMillis()
        val userMessage = ButlerChatMessage(
            id = "mock-user-$now",
            role = ButlerMessageRole.User,
            content = message.trim(),
            createdAtEpochMillis = now,
        )
        val assistantMessage = ButlerChatMessage(
            id = "mock-assistant-$now",
            role = ButlerMessageRole.Assistant,
            content = "I can help refine this trip once the native Butler bridge is active.",
            createdAtEpochMillis = now + 1,
        )
        messages.update { it + userMessage + assistantMessage }
        return ButlerTurnResult(
            assistantMessage = assistantMessage,
            trip = activeTrip.value ?: MockTripData.initialTripState,
            suggestions = listOf("Make this easier to follow", "Show me today's next step"),
            modelLabel = "mock fallback",
            offlineFallback = true,
        )
    }

    override suspend fun renameActiveTrip(newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        activeTrip.update { trip ->
            trip?.copy(summary = trip.summary.copy(title = trimmed))
        }
    }

    override suspend fun setAlertDone(alert: ButlerAlert, done: Boolean) {
        activeTrip.update { trip ->
            trip?.copy(
                alerts = trip.alerts.map { current ->
                    if (current == alert) current.copy(done = done) else current
                },
            )
        }
    }
}
