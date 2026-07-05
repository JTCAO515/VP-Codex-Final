package space.go2china.visepanda.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("visepanda_sync_prefs", Context.MODE_PRIVATE)

    fun getCloudTripId(): String? {
        return prefs.getString("cloud_trip_id", null)
    }

    fun saveCloudTripId(tripId: String) {
        prefs.edit().putString("cloud_trip_id", tripId).apply()
    }

    fun getPersistedMessageCount(): Int {
        return prefs.getInt("persisted_message_count", 0)
    }

    fun savePersistedMessageCount(count: Int) {
        prefs.edit().putInt("persisted_message_count", count).apply()
    }

    fun clearSyncState() {
        prefs.edit().clear().apply()
    }
}
