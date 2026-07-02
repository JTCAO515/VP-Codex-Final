package space.go2china.visepanda.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TripCacheEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class VisePandaDatabase : RoomDatabase() {
    abstract fun tripCacheDao(): TripCacheDao
}
