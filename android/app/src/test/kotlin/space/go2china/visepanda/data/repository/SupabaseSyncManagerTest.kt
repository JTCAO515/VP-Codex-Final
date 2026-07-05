package space.go2china.visepanda.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import space.go2china.visepanda.data.local.AuthPreferences
import space.go2china.visepanda.data.local.SyncPreferences
import space.go2china.visepanda.data.local.TripCacheDao
import space.go2china.visepanda.data.local.TripCacheEntity
import space.go2china.visepanda.data.model.*
import space.go2china.visepanda.data.remote.*
import space.go2china.visepanda.data.serialization.TripJson

/**
 * Architect takeover (2026-07-05): #44's original submission always saved
 * `messages.size` as the persisted count after the message-sync loop, even
 * when a message insert failed partway through and the loop broke early —
 * meaning the failed message (and everything queued after it) was silently
 * marked as synced and would never be retried. These tests pin the fixed
 * behavior down: only messages that actually got a successful response
 * advance the persisted count.
 */
class SupabaseSyncManagerTest {

    private class FakeAuthPreferences(
        private val accessToken: String? = "token",
        private val userId: String? = "user-1",
        private val email: String? = "traveler@example.com",
    ) : AuthPreferences {
        override fun saveSession(accessToken: String, refreshToken: String, email: String, userId: String) {}
        override fun getAccessToken(): String? = accessToken
        override fun getRefreshToken(): String? = null
        override fun getEmail(): String? = email
        override fun getUserId(): String? = userId
        override fun clearSession() {}
    }

    private class FakeSyncPreferences : SyncPreferences {
        var storedCloudTripId: String? = null
        var storedPersistedMessageCount: Int = 0
        var cleared = false

        override fun getCloudTripId(): String? = storedCloudTripId
        override fun saveCloudTripId(tripId: String) {
            storedCloudTripId = tripId
        }
        override fun getPersistedMessageCount(): Int = storedPersistedMessageCount
        override fun savePersistedMessageCount(count: Int) {
            storedPersistedMessageCount = count
        }
        override fun clearSyncState() {
            cleared = true
            storedCloudTripId = null
            storedPersistedMessageCount = 0
        }
    }

    private class FakeTripCacheDao(private val entity: TripCacheEntity?) : TripCacheDao {
        override fun observe(id: String) = throw NotImplementedError("unused in this test")
        override suspend fun get(id: String): TripCacheEntity? = entity
        override suspend fun upsert(entity: TripCacheEntity) {}
        override suspend fun delete(id: String) {}
    }

    /** Fails the Nth message insert (1-indexed) and succeeds on everything else. */
    private class FakeSupabaseTripApiService(private val failOnMessageNumber: Int? = null) : SupabaseTripApiService {
        var messageInsertCount = 0

        override suspend fun upsertUser(apiKey: String, authorization: String, prefer: String, body: SupabaseUserBody) =
            Response.success(Unit)

        override suspend fun insertTrip(apiKey: String, authorization: String, prefer: String, body: SupabaseTripInsertBody) =
            Response.success(Unit)

        override suspend fun patchTrip(apiKey: String, authorization: String, idFilter: String, body: SupabaseTripPatchBody) =
            Response.success(Unit)

        override suspend fun insertCanvasVersion(apiKey: String, authorization: String, prefer: String, body: SupabaseCanvasVersionBody) =
            Response.success(Unit)

        override suspend fun insertMessage(apiKey: String, authorization: String, prefer: String, body: SupabaseMessageBody): Response<Unit> {
            messageInsertCount++
            if (failOnMessageNumber == messageInsertCount) {
                return Response.error(500, okhttp3.ResponseBody.create(null, "boom"))
            }
            return Response.success(Unit)
        }
    }

    private fun sampleTrip(): TripState = TripState(
        summary = TripSummary(
            title = "Beijing Trip",
            durationDays = 1,
            pace = Pace.Balanced,
            travelerStyle = "First-time visitor",
            destinations = listOf("Beijing"),
            confidence = TripConfidence.Draft,
        ),
        days = emptyList(),
        alerts = emptyList(),
        lastUpdatedReason = "Initial draft.",
    )

    private fun message(id: String, epochMillis: Long) = ButlerChatMessage(
        id = id,
        role = ButlerMessageRole.User,
        content = "hello $id",
        createdAtEpochMillis = epochMillis,
    )

    private fun entityWithMessages(messages: List<ButlerChatMessage>): TripCacheEntity = TripCacheEntity(
        id = "active",
        tripStateJson = TripJson.encodeTrip(sampleTrip()),
        messagesJson = TripJson.encodeMessages(messages),
        updatedAtEpochMillis = 0,
    )

    /** Polls until [condition] is true or the timeout elapses — triggerSync() is fire-and-forget. */
    private fun awaitUntil(timeoutMs: Long = 2000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(10)
        }
        assertTrue("condition did not become true within ${timeoutMs}ms", condition())
    }

    @Test
    fun doesNotSyncWhenNotSignedIn() = runBlocking {
        val api = FakeSupabaseTripApiService()
        val prefs = FakeSyncPreferences()
        val manager = LiveSupabaseSyncManager(
            FakeAuthPreferences(accessToken = null, userId = null),
            prefs,
            FakeTripCacheDao(null),
            api,
        )

        manager.triggerSync()
        Thread.sleep(100)

        assertEquals(SyncStatus.NOT_SIGNED_IN, manager.syncStatus.value)
        assertEquals(0, api.messageInsertCount)
    }

    @Test
    fun successfulSyncPersistsAllMessages() {
        val api = FakeSupabaseTripApiService()
        val prefs = FakeSyncPreferences()
        val entity = entityWithMessages(listOf(message("m1", 1), message("m2", 2), message("m3", 3)))
        val manager = LiveSupabaseSyncManager(FakeAuthPreferences(), prefs, FakeTripCacheDao(entity), api)

        manager.triggerSync()

        awaitUntil { manager.syncStatus.value == SyncStatus.SYNCED }
        assertEquals(3, api.messageInsertCount)
        assertEquals(3, prefs.storedPersistedMessageCount)
        assertTrue(prefs.storedCloudTripId != null)
    }

    @Test
    fun partialMessageFailureDoesNotAdvancePersistedCountPastTheFailure() {
        // Regression test for the bug found during architect takeover: message
        // #2 of 3 fails, so only message #1 should count as persisted — #2 and
        // #3 must be retried on the next sync, not silently marked done.
        val api = FakeSupabaseTripApiService(failOnMessageNumber = 2)
        val prefs = FakeSyncPreferences()
        val entity = entityWithMessages(listOf(message("m1", 1), message("m2", 2), message("m3", 3)))
        val manager = LiveSupabaseSyncManager(FakeAuthPreferences(), prefs, FakeTripCacheDao(entity), api)

        manager.triggerSync()

        awaitUntil { api.messageInsertCount >= 2 }
        Thread.sleep(50) // let the coroutine finish updating state after the break
        assertEquals(1, prefs.storedPersistedMessageCount)
        assertEquals(SyncStatus.SYNCED, manager.syncStatus.value) // trip itself still synced even if messages lag
    }

    @Test
    fun retryAfterPartialFailureOnlyResendsTheUnsyncedTail() {
        val api = FakeSupabaseTripApiService(failOnMessageNumber = 2)
        val prefs = FakeSyncPreferences()
        val entity = entityWithMessages(listOf(message("m1", 1), message("m2", 2), message("m3", 3)))
        val manager = LiveSupabaseSyncManager(FakeAuthPreferences(), prefs, FakeTripCacheDao(entity), api)

        manager.triggerSync()
        awaitUntil { api.messageInsertCount >= 2 }
        Thread.sleep(50)
        assertEquals(1, prefs.storedPersistedMessageCount)

        // Second sync attempt, this time nothing fails — should only resend m2 and m3.
        val api2 = FakeSupabaseTripApiService()
        val manager2 = LiveSupabaseSyncManager(FakeAuthPreferences(), prefs, FakeTripCacheDao(entity), api2)
        manager2.triggerSync()

        awaitUntil { manager2.syncStatus.value == SyncStatus.SYNCED }
        assertEquals(2, api2.messageInsertCount) // only m2 + m3, not m1 again
        assertEquals(3, prefs.storedPersistedMessageCount)
    }

    @Test
    fun clearSyncStateResetsCloudTripAndMessageCount() {
        val prefs = FakeSyncPreferences().apply {
            storedCloudTripId = "trip-1"
            storedPersistedMessageCount = 5
        }
        val manager = LiveSupabaseSyncManager(FakeAuthPreferences(), prefs, FakeTripCacheDao(null), FakeSupabaseTripApiService())

        manager.clearSyncState()

        assertTrue(prefs.cleared)
    }
}
