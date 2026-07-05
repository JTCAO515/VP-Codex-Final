package space.go2china.visepanda.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import space.go2china.visepanda.data.model.MemoryDeleteResponse
import space.go2china.visepanda.data.model.MemoryEntry
import space.go2china.visepanda.data.model.MemoryProfileResponse
import space.go2china.visepanda.data.remote.MemoryApiService

class MemoryRepositoryTest {

    private class MockMemoryApiService(
        private val success: Boolean,
        private val entries: List<MemoryEntry> = emptyList(),
        private val removed: Boolean = true,
    ) : MemoryApiService {
        override suspend fun getProfile(userId: String): MemoryProfileResponse {
            if (!success) throw Exception("Network failure")
            return MemoryProfileResponse(ok = true, entries = entries)
        }

        override suspend fun deleteEntry(userId: String, key: String, value: String): MemoryDeleteResponse {
            if (!success) throw Exception("Network failure")
            return MemoryDeleteResponse(ok = true, removed = removed)
        }
    }

    @Test
    fun fetchProfile_success_returnsEntries() = runBlocking {
        val entry = MemoryEntry(key = "dietary", value = "vegetarian", confidence = 0.9, evidence = listOf("I am vegetarian"))
        val repository = LiveMemoryRepository(MockMemoryApiService(success = true, entries = listOf(entry)))

        val result = repository.fetchProfile("guest-1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("dietary", result.getOrNull()?.first()?.key)
    }

    @Test
    fun fetchProfile_failure_returnsFailureResult() = runBlocking {
        val repository = LiveMemoryRepository(MockMemoryApiService(success = false))

        val result = repository.fetchProfile("guest-1")

        assertTrue(result.isFailure)
    }

    @Test
    fun deleteEntry_success_returnsRemoved() = runBlocking {
        val repository = LiveMemoryRepository(MockMemoryApiService(success = true, removed = true))

        val result = repository.deleteEntry("guest-1", "dietary", "vegetarian")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun deleteEntry_failure_returnsFailureResult() = runBlocking {
        val repository = LiveMemoryRepository(MockMemoryApiService(success = false))

        val result = repository.deleteEntry("guest-1", "dietary", "vegetarian")

        assertTrue(result.isFailure)
    }
}
