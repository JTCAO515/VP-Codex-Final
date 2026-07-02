package space.go2china.visepanda.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Not yet consumed by [space.go2china.visepanda.data.repository.MockTripRepository] —
 * see that class's doc comment. Defined now so the v0.3.5 sync bridge has an
 * existing, reviewed DAO shape to build against instead of designing Room
 * access patterns under the same deadline as the Supabase bridge itself.
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
