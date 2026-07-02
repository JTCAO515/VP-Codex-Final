package space.go2china.visepanda.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * v0.3.3 intentionally keeps local persistence coarse-grained: the entire
 * [space.go2china.visepanda.data.model.TripState] for the active trip is
 * stored as one serialized JSON blob in a single row, not normalized into
 * per-day/per-block tables.
 *
 * This is a deliberate scope decision, not an oversight: normalizing the
 * schema only pays off once real sync/merge logic exists (v0.3.5), and
 * guessing at that schema now — before the Supabase bridge and conflict
 * rules are designed — risks a migration before the app has any real users.
 * A single JSON column is trivial to migrate away from later.
 */
@Entity(tableName = "trip_cache")
data class TripCacheEntity(
    @PrimaryKey val id: String,
    val tripStateJson: String,
    val messagesJson: String,
    val updatedAtEpochMillis: Long,
)
