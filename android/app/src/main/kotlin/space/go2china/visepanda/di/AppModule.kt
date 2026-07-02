package space.go2china.visepanda.di

import android.content.Context
import androidx.room.Room
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import space.go2china.visepanda.BuildConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import space.go2china.visepanda.data.local.TripCacheDao
import space.go2china.visepanda.data.local.VisePandaDatabase
import space.go2china.visepanda.data.remote.ButlerApiService
import space.go2china.visepanda.data.repository.RoomTripRepository
import space.go2china.visepanda.data.repository.TripRepository
import space.go2china.visepanda.data.serialization.TripJson

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * v0.3.6 implements the previously planned v0.3.5 Butler + Sync Bridge:
     * screens still depend only on [TripRepository], while this binding now
     * points at the Room-backed native bridge.
     */
    @Binds
    @Singleton
    abstract fun bindTripRepository(impl: RoomTripRepository): TripRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VisePandaDatabase =
        Room.databaseBuilder(context, VisePandaDatabase::class.java, "visepanda.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTripCacheDao(database: VisePandaDatabase): TripCacheDao = database.tripCacheDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.VISEPANDA_API_BASE_URL.ensureTrailingSlash())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(TripJson.gson))
            .build()

    @Provides
    @Singleton
    fun provideButlerApiService(retrofit: Retrofit): ButlerApiService =
        retrofit.create(ButlerApiService::class.java)

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"
}
