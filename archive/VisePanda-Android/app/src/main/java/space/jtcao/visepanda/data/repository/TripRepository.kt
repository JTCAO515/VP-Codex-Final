package space.jtcao.visepanda.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import space.jtcao.visepanda.data.model.Trip
import java.util.UUID

/** DataStore extension property for trips storage */
private val Context.tripsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vise_panda_trips"
)

/**
 * Repository for saved trips — stored locally via DataStore.
 *
 * Trips are serialized as JSON strings and stored in a single preferences key.
 * This keeps the implementation simple (no Room DB needed for MVP).
 */
class TripRepository(private val context: Context) {

    companion object {
        private val TRIPS_KEY = stringPreferencesKey("saved_trips")
    }

    private val json = Json { ignoreUnknownKeys = true }

    /** Get all saved trips as a Flow */
    fun getAllTrips(): Flow<List<Trip>> {
        return context.tripsDataStore.data.map { prefs ->
            val raw = prefs[TRIPS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Trip>>(raw)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /** Save (create or update) a trip */
    suspend fun saveTrip(trip: Trip) {
        context.tripsDataStore.edit { prefs ->
            val raw = prefs[TRIPS_KEY] ?: "[]"
            val trips = try {
                json.decodeFromString<MutableList<Trip>>(raw)
            } catch (e: Exception) {
                mutableListOf()
            }
            val index = trips.indexOfFirst { it.id == trip.id }
            if (index >= 0) {
                trips[index] = trip.copy(updatedAt = System.currentTimeMillis())
            } else {
                trips.add(
                    trip.copy(
                        id = if (trip.id.isBlank()) UUID.randomUUID().toString() else trip.id,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            prefs[TRIPS_KEY] = json.encodeToString(trips)
        }
    }

    /** Delete a trip by ID */
    suspend fun deleteTrip(tripId: String) {
        context.tripsDataStore.edit { prefs ->
            val raw = prefs[TRIPS_KEY] ?: "[]"
            val trips = try {
                json.decodeFromString<MutableList<Trip>>(raw)
            } catch (e: Exception) {
                mutableListOf()
            }
            trips.removeAll { it.id == tripId }
            prefs[TRIPS_KEY] = json.encodeToString(trips)
        }
    }
}
