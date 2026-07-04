package space.go2china.visepanda.data.repository

import kotlinx.coroutines.flow.Flow
import space.go2china.visepanda.data.model.ButlerAlert
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.ButlerTurnResult
import space.go2china.visepanda.data.model.TripState

/**
 * Offline-first trip data boundary — see
 * docs/planning/v0.3.2-android-planning-synthesis.md "State Layers".
 *
 * v0.3.6 adds the native Butler bridge behind this interface: a Room-backed
 * active trip cache plus a `/api/chat` client with local mock fallback. UI code
 * should only ever depend on this contract, never on a concrete repository.
 * Supabase auth/trips/messages parity remains the next bridge layer rather
 * than a schema change in this native round.
 */
interface TripRepository {

    /** The traveler's active trip, or null if none exists yet (fresh install, no chat started). */
    fun observeActiveTrip(): Flow<TripState?>

    /**
     * True while the repository believes it is working from a stale/local-only
     * copy (no network, or sync has not happened yet). Today/Plan screens use
     * this to show the offline banner honestly instead of pretending the data
     * is live.
     */
    fun observeOffline(): Flow<Boolean>

    /** The active native Butler conversation, cached locally with the active trip. */
    fun observeButlerMessages(): Flow<List<ButlerChatMessage>>

    /**
     * Sends a user message through the native Butler bridge. The implementation
     * should try the existing `/api/chat` route first, then degrade to a local
     * mock patch if the network or server is unavailable.
     */
    suspend fun sendButlerMessage(message: String): ButlerTurnResult

    /** Local-only rename — not itinerary content, does not go through the Butler pipeline. */
    suspend fun renameActiveTrip(newTitle: String)

    /** Local-only checklist toggle — not itinerary content, does not go through the Butler pipeline. */
    suspend fun setAlertDone(alert: ButlerAlert, done: Boolean)

    /** Sets the pending Explore POI payload to be sent with the next chat request. */
    fun setPendingExplorePoi(message: String, payload: String)

    /** Gets the pending Explore POI message, or null if none. */
    fun getPendingExplorePoiMessage(): String?

    /** Clears the pending Explore POI. */
    fun clearPendingExplorePoi()
}
