package space.go2china.visepanda.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.go2china.visepanda.data.local.AuthPreferences
import space.go2china.visepanda.data.local.SyncPreferences
import space.go2china.visepanda.data.local.TripCacheDao
import space.go2china.visepanda.data.model.TripState
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.ButlerMessageRole
import space.go2china.visepanda.data.remote.SupabaseConfig
import space.go2china.visepanda.data.remote.SupabaseTripApiService
import space.go2china.visepanda.data.remote.SupabaseUserBody
import space.go2china.visepanda.data.remote.SupabaseTripInsertBody
import space.go2china.visepanda.data.remote.SupabaseTripPatchBody
import space.go2china.visepanda.data.remote.SupabaseCanvasVersionBody
import space.go2china.visepanda.data.remote.SupabaseMessageBody
import space.go2china.visepanda.data.serialization.TripJson
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncStatus {
    NOT_SIGNED_IN,
    NOT_SYNCED,
    SYNCING,
    SYNCED,
    FAILED
}

interface SupabaseSyncManager {
    val syncStatus: StateFlow<SyncStatus>
    fun triggerSync()
    fun clearSyncState()
}

@Singleton
class LiveSupabaseSyncManager @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val syncPreferences: SyncPreferences,
    private val tripCacheDao: TripCacheDao,
    private val apiService: SupabaseTripApiService
) : SupabaseSyncManager {

    private val _syncStatus = MutableStateFlow<SyncStatus>(
        if (authPreferences.getAccessToken() != null) SyncStatus.NOT_SYNCED else SyncStatus.NOT_SIGNED_IN
    )
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus

    private val mutex = Mutex()
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun triggerSync() {
        val accessToken = authPreferences.getAccessToken()
        val userId = authPreferences.getUserId()
        val email = authPreferences.getEmail()

        if (accessToken == null || userId == null) {
            _syncStatus.value = SyncStatus.NOT_SIGNED_IN
            syncPreferences.clearSyncState()
            return
        }

        syncScope.launch {
            mutex.withLock {
                _syncStatus.value = SyncStatus.SYNCING
                try {
                    // 1. 获取本地活跃行程
                    val entity = tripCacheDao.get("active")
                    if (entity == null) {
                        // 本地还没有活跃行程，直接完成
                        _syncStatus.value = SyncStatus.SYNCED
                        return@launch
                    }

                    val tripState = runCatching { TripJson.decodeTrip(entity.tripStateJson) }.getOrNull()
                    val messages = runCatching { TripJson.decodeMessages(entity.messagesJson) }.getOrNull().orEmpty()

                    if (tripState == null) {
                        _syncStatus.value = SyncStatus.FAILED
                        return@launch
                    }

                    val authHeader = "Bearer $accessToken"
                    val anonKey = SupabaseConfig.SUPABASE_ANON_KEY

                    // A. Upsert User 确保外键依赖
                    if (email != null) {
                        val userRes = apiService.upsertUser(
                            apiKey = anonKey,
                            authorization = authHeader,
                            body = SupabaseUserBody(id = userId, email = email)
                        )
                        if (!userRes.isSuccessful) {
                            Log.w("SupabaseSyncManager", "Upsert user failed: ${userRes.code()}")
                        }
                    }

                    // B. Trips 表映射
                    var cloudTripId = syncPreferences.getCloudTripId()
                    val isNewTrip = cloudTripId == null
                    if (isNewTrip) {
                        val newId = UUID.randomUUID().toString()
                        val tripInsertRes = apiService.insertTrip(
                            apiKey = anonKey,
                            authorization = authHeader,
                            body = SupabaseTripInsertBody(
                                id = newId,
                                owner_id = userId,
                                title = tripState.summary.title,
                                status = when (tripState.summary.confidence) {
                                    space.go2china.visepanda.data.model.TripConfidence.ReadyToSave -> "ready"
                                    else -> "draft"
                                }
                            )
                        )
                        if (!tripInsertRes.isSuccessful) {
                            throw IllegalStateException("Insert trip failed: ${tripInsertRes.code()}")
                        }
                        cloudTripId = newId
                        syncPreferences.saveCloudTripId(newId)
                    } else {
                        val tripPatchRes = apiService.patchTrip(
                            apiKey = anonKey,
                            authorization = authHeader,
                            idFilter = "eq.$cloudTripId",
                            body = SupabaseTripPatchBody(
                                title = tripState.summary.title,
                                status = when (tripState.summary.confidence) {
                                    space.go2china.visepanda.data.model.TripConfidence.ReadyToSave -> "ready"
                                    else -> "draft"
                                },
                                updated_at = Instant.ofEpochMilli(System.currentTimeMillis()).toString()
                            )
                        )
                        if (!tripPatchRes.isSuccessful) {
                            throw IllegalStateException("Patch trip failed: ${tripPatchRes.code()}")
                        }
                    }

                    // C. Canvas Version 映射
                    val canvasVersionId = UUID.randomUUID().toString()
                    val canvasRes = apiService.insertCanvasVersion(
                        apiKey = anonKey,
                        authorization = authHeader,
                        body = SupabaseCanvasVersionBody(
                            id = canvasVersionId,
                            trip_id = cloudTripId!!,
                            canvas = tripState,
                            last_updated_reason = tripState.lastUpdatedReason
                        )
                    )
                    if (!canvasRes.isSuccessful) {
                        throw IllegalStateException("Insert canvas version failed: ${canvasRes.code()}")
                    }

                    // D. 关联 current_canvas_version_id 到 trips 表
                    val linkRes = apiService.patchTrip(
                        apiKey = anonKey,
                        authorization = authHeader,
                        idFilter = "eq.$cloudTripId",
                        body = SupabaseTripPatchBody(
                            current_canvas_version_id = canvasVersionId
                        )
                    )
                    if (!linkRes.isSuccessful) {
                        throw IllegalStateException("Link canvas version failed: ${linkRes.code()}")
                    }

                    // E. Messages 增量同步
                    val persistedMessageCount = syncPreferences.getPersistedMessageCount()
                    if (messages.size > persistedMessageCount) {
                        val unsyncedMessages = messages.subList(persistedMessageCount, messages.size)
                        // Bug fix (architect takeover, 2026-07-05): the previous version always
                        // saved messages.size as the persisted count after the loop, even when a
                        // message insert failed and the loop broke early — meaning a failed
                        // message (and everything after it) was silently marked as synced and
                        // would never be retried. Track how many actually succeeded instead.
                        var syncedCount = persistedMessageCount
                        for (msg in unsyncedMessages) {
                            val msgRes = apiService.insertMessage(
                                apiKey = anonKey,
                                authorization = authHeader,
                                body = SupabaseMessageBody(
                                    id = msg.id,
                                    trip_id = cloudTripId,
                                    role = when (msg.role) {
                                        ButlerMessageRole.User -> "user"
                                        ButlerMessageRole.Assistant -> "assistant"
                                    },
                                    content = msg.content,
                                    created_at = Instant.ofEpochMilli(msg.createdAtEpochMillis).toString()
                                )
                            )
                            if (!msgRes.isSuccessful) {
                                Log.e("SupabaseSyncManager", "Insert message failed: ${msgRes.code()}")
                                // Stop here — do not advance syncedCount past this message, so it
                                // (and anything after it) is retried on the next sync.
                                break
                            }
                            syncedCount++
                        }
                        syncPreferences.savePersistedMessageCount(syncedCount)
                    }

                    _syncStatus.value = SyncStatus.SYNCED
                } catch (e: Exception) {
                    Log.e("SupabaseSyncManager", "Sync error", e)
                    _syncStatus.value = SyncStatus.FAILED
                }
            }
        }
    }

    override fun clearSyncState() {
        syncPreferences.clearSyncState()
        _syncStatus.value = if (authPreferences.getAccessToken() != null) SyncStatus.NOT_SYNCED else SyncStatus.NOT_SIGNED_IN
    }
}
