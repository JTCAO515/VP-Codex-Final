package space.go2china.visepanda.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Consumed by the native Butler bridge from v0.3.6 onward. The cache remains
 * coarse-grained on purpose: one active trip JSON blob plus its chat transcript
 * is enough for offline continuity while Supabase merge rules stay out of this
 * round's scope.
 */
@Dao
interface TripCacheDao {

    @Query("SELECT * FROM trip_cache WHERE id = :id LIMIT 1")
    fun observe(id: String): Flow<TripCacheEntity?>

    @Query("SELECT * FROM trip_cache WHERE id = :id LIMIT 1")
    suspend fun get(id: String): TripCacheEntity?

    @Upsert
    suspend fun upsert(entity: TripCacheEntity)

    @Query("DELETE FROM trip_cache WHERE id = :id")
    suspend fun delete(id: String)
}
