package space.go2china.visepanda.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Interface + Live impl split (architect takeover, 2026-07-05) — matches the
// AuthPreferences pattern so SupabaseSyncManager can be unit-tested with a
// fake instead of needing a real android.content.Context.
interface SyncPreferences {
    fun getCloudTripId(): String?
    fun saveCloudTripId(tripId: String)
    fun getPersistedMessageCount(): Int
    fun savePersistedMessageCount(count: Int)
    fun clearSyncState()
}

@Singleton
class SharedPrefsSyncPreferences @Inject constructor(
    @ApplicationContext context: Context
) : SyncPreferences {
    private val prefs = context.getSharedPreferences("visepanda_sync_prefs", Context.MODE_PRIVATE)

    override fun getCloudTripId(): String? {
        return prefs.getString("cloud_trip_id", null)
    }

    override fun saveCloudTripId(tripId: String) {
        prefs.edit().putString("cloud_trip_id", tripId).apply()
    }

    override fun getPersistedMessageCount(): Int {
        return prefs.getInt("persisted_message_count", 0)
    }

    override fun savePersistedMessageCount(count: Int) {
        prefs.edit().putInt("persisted_message_count", count).apply()
    }

    override fun clearSyncState() {
        prefs.edit().clear().apply()
    }
}
