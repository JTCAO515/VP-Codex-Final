package space.go2china.visepanda.data.repository

import kotlinx.coroutines.flow.Flow
import space.go2china.visepanda.data.model.ButlerAlert
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.ButlerTurnResult
import space.go2china.visepanda.data.model.TripBlock
import space.go2china.visepanda.data.model.TripState

/**
 * Offline-first trip data boundary — see
 * docs/planning/v0.3.2-android-planning-synthesis.md "State Layers".
 *
 * v0.3.6 adds the native Butler bridge behind this interface: a Room-backed
 * active trip cache plus a `/api/chat` client. UI code
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
     * Sends a user message through the native Butler bridge. Network or server
     * failures surface as errors instead of changing the itinerary locally.
     */
    suspend fun sendButlerMessage(message: String): ButlerTurnResult

    /** Local-only rename — not itinerary content, does not go through the Butler pipeline. */
    suspend fun renameActiveTrip(newTitle: String)

    /** Local-only checklist toggle — not itinerary content, does not go through the Butler pipeline. */
    suspend fun setAlertDone(alert: ButlerAlert, done: Boolean)

    /**
     * v0.3.14: Explore's "Add to Trip" — appends a POI-derived block to the
     * given day's itinerary directly (local-only mutation, same category as
     * [renameActiveTrip]/[setAlertDone] — does not go through the Butler
     * pipeline since the user picked this POI themselves, not the AI).
     */
    suspend fun addPoiToDay(dayNumber: Int, block: TripBlock)

    /** DayDetail's inline description edit — local-only, same category as [addPoiToDay]. */
    suspend fun updateBlockDescription(dayNumber: Int, blockIndex: Int, newDescription: String)

    /** DayDetail's reorder-within-day control — local-only, same category as [addPoiToDay]. */
    suspend fun moveBlock(dayNumber: Int, fromIndex: Int, toIndex: Int)
}
