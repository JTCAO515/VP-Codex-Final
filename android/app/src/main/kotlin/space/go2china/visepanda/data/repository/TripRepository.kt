package space.go2china.visepanda.data.repository

import kotlinx.coroutines.flow.Flow
import space.go2china.visepanda.data.model.ButlerAlert
import space.go2china.visepanda.data.model.TripState

/**
 * Offline-first trip data boundary — see
 * docs/planning/v0.3.2-android-planning-synthesis.md "State Layers".
 *
 * v0.3.3/v0.3.4 only have [MockTripRepository] behind this interface: a
 * single in-memory trip seeded from [space.go2china.visepanda.data.model.MockTripData],
 * with local-only mutations for the two known non-content operations
 * (renaming and alert-done toggling — see AGENTS.md v0.2.7 note on which
 * mutations are allowed to bypass the Butler pipeline).
 *
 * v0.3.5 (Butler + Sync Bridge) is expected to add a real implementation
 * backed by Room (durable local cache) with Supabase as the canonical
 * remote source after login, without changing this interface's shape — UI
 * code should only ever depend on this contract, never on
 * [MockTripRepository] directly.
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

    /** Local-only rename — not itinerary content, does not go through the Butler pipeline. */
    suspend fun renameActiveTrip(newTitle: String)

    /** Local-only checklist toggle — not itinerary content, does not go through the Butler pipeline. */
    suspend fun setAlertDone(alert: ButlerAlert, done: Boolean)
}
