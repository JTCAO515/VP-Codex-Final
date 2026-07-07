package space.go2china.visepanda.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: androidx.datastore.core.DataStore<Preferences> by
    preferencesDataStore(name = "visepanda_user_prefs")

/**
 * Lightweight, non-itinerary settings only — see
 * docs/planning/v0.3.2-android-planning-synthesis.md "State Layers", layer 3.
 * Never store trip content here; that belongs in [space.go2china.visepanda.data.local.TripCacheEntity]
 * (Room) or the future Supabase-backed remote source.
 *
 * v0.3.14 adds [languageCode] (Me screen's language switch, /goal Phase 3) —
 * the guest-profile keys mentioned in the original v0.3.3 note still wait for
 * a real login flow.
 */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val lastActiveTripIdKey = stringPreferencesKey("last_active_trip_id")
    private val languageCodeKey = stringPreferencesKey("language_code")

    val lastActiveTripId: Flow<String?> =
        context.userPreferencesDataStore.data.map { prefs -> prefs[lastActiveTripIdKey] }

    suspend fun setLastActiveTripId(tripId: String) {
        context.userPreferencesDataStore.edit { prefs -> prefs[lastActiveTripIdKey] = tripId }
    }

    /** BCP-47 language tag, e.g. "en" or "zh-CN". Defaults to "en" when unset. */
    val languageCode: Flow<String> =
        context.userPreferencesDataStore.data.map { prefs -> prefs[languageCodeKey] ?: "en" }

    suspend fun setLanguageCode(code: String) {
        context.userPreferencesDataStore.edit { prefs -> prefs[languageCodeKey] = code }
    }
}
