package space.go2china.visepanda.di

import android.content.Context
import androidx.room.Room
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
import space.go2china.visepanda.data.local.AuthPreferences
import space.go2china.visepanda.data.local.SharedPrefsAuthPreferences
import space.go2china.visepanda.data.remote.ButlerApiService
import space.go2china.visepanda.data.remote.ExchangeRateApiService
import space.go2china.visepanda.data.remote.ExploreApiService
import space.go2china.visepanda.data.remote.TranslateApiService
import space.go2china.visepanda.data.remote.AuthApiService
import space.go2china.visepanda.data.remote.SupabaseConfig
import space.go2china.visepanda.data.repository.ExploreRepository
import space.go2china.visepanda.data.repository.LiveExploreRepository
import space.go2china.visepanda.data.repository.LiveToolsRepository
import space.go2china.visepanda.data.repository.RoomTripRepository
import space.go2china.visepanda.data.repository.ToolsRepository
import space.go2china.visepanda.data.repository.TranslateRepository
import space.go2china.visepanda.data.repository.LiveTranslateRepository
import space.go2china.visepanda.data.repository.TripRepository
import space.go2china.visepanda.data.repository.AuthRepository
import space.go2china.visepanda.data.repository.LiveAuthRepository
import space.go2china.visepanda.data.serialization.TripJson
import space.go2china.visepanda.data.remote.SupabaseTripApiService
import space.go2china.visepanda.data.repository.SupabaseSyncManager
import space.go2china.visepanda.data.repository.LiveSupabaseSyncManager


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

    @Binds
    @Singleton
    abstract fun bindExploreRepository(impl: LiveExploreRepository): ExploreRepository

    /** v0.3.13: checklist content + live exchange-rate merge — see DESIGN.md ADR-117. */
    @Binds
    @Singleton
    abstract fun bindToolsRepository(impl: LiveToolsRepository): ToolsRepository

    @Binds
    @Singleton
    abstract fun bindTranslateRepository(impl: LiveTranslateRepository): TranslateRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: LiveAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAuthPreferences(impl: SharedPrefsAuthPreferences): AuthPreferences

    @Binds
    @Singleton
    abstract fun bindSupabaseSyncManager(impl: LiveSupabaseSyncManager): SupabaseSyncManager
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

    /**
     * v0.3.12: the default OkHttp 10s read timeout was silently killing every
     * real chat request — the deployed orchestrator races up to 6 providers
     * in parallel and a measured real request took ~14.4s end to end, so
     * every native chat message timed out even though the backend and
     * request/response contract were both correct.
     * See DESIGN.md ADR-116. 45s read gives headroom above both the observed
     * latency and the orchestrator's own ~18s per-provider timeout.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
                )
            }
        }
        .build()

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

    @Provides
    @Singleton
    fun provideExchangeRateApiService(retrofit: Retrofit): ExchangeRateApiService =
        retrofit.create(ExchangeRateApiService::class.java)

    @Provides
    @Singleton
    fun provideTranslateApiService(retrofit: Retrofit): TranslateApiService =
        retrofit.create(TranslateApiService::class.java)

    @Provides
    @Singleton
    fun provideExploreApiService(retrofit: Retrofit): ExploreApiService =
        retrofit.create(ExploreApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(okHttpClient: OkHttpClient): AuthApiService {
        val baseUrl = if (SupabaseConfig.SUPABASE_URL.endsWith("/")) {
            SupabaseConfig.SUPABASE_URL
        } else {
            "${SupabaseConfig.SUPABASE_URL}/"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(TripJson.gson))
            .build()
            .create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSupabaseTripApiService(okHttpClient: OkHttpClient): SupabaseTripApiService {
        val baseUrl = if (SupabaseConfig.SUPABASE_URL.endsWith("/")) {
            SupabaseConfig.SUPABASE_URL
        } else {
            "${SupabaseConfig.SUPABASE_URL}/"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(TripJson.gson))
            .build()
            .create(SupabaseTripApiService::class.java)
    }

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"
}
