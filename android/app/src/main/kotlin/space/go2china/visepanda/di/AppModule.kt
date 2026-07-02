package space.go2china.visepanda.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import space.go2china.visepanda.data.local.TripCacheDao
import space.go2china.visepanda.data.local.VisePandaDatabase
import space.go2china.visepanda.data.repository.MockTripRepository
import space.go2china.visepanda.data.repository.TripRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the mock implementation until v0.3.5 introduces a real
     * Room-backed / Supabase-synced [TripRepository]. Swapping the binding
     * here is meant to be the only change needed at that point — screens
     * depend on the interface, never on [MockTripRepository] directly.
     */
    @Binds
    @Singleton
    abstract fun bindTripRepository(impl: MockTripRepository): TripRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VisePandaDatabase =
        Room.databaseBuilder(context, VisePandaDatabase::class.java, "visepanda.db").build()

    @Provides
    fun provideTripCacheDao(database: VisePandaDatabase): TripCacheDao = database.tripCacheDao()
}
