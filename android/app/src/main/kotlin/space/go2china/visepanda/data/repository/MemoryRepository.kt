package space.go2china.visepanda.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import space.go2china.visepanda.data.model.MemoryEntry
import space.go2china.visepanda.data.remote.MemoryApiService

/**
 * "AI Profile" view/delete surface (Issue #85, mirrors iOS MeView.swift's
 * memoryProfileSection) — lets the traveler see and remove entries the
 * memory system inferred or was explicitly told, backed by butler-service's
 * GET/DELETE /butler/memory/profile.
 */
interface MemoryRepository {
    suspend fun fetchProfile(userId: String): Result<List<MemoryEntry>>
    suspend fun deleteEntry(userId: String, key: String, value: String): Result<Boolean>
}

@Singleton
class LiveMemoryRepository @Inject constructor(
    private val memoryApiService: MemoryApiService,
) : MemoryRepository {

    override suspend fun fetchProfile(userId: String): Result<List<MemoryEntry>> {
        return runCatching {
            val response = memoryApiService.getProfile(userId)
            if (response.ok) {
                response.entries
            } else {
                throw Exception(response.error ?: "Could not reach /butler/memory/profile")
            }
        }
    }

    override suspend fun deleteEntry(userId: String, key: String, value: String): Result<Boolean> {
        return runCatching {
            val response = memoryApiService.deleteEntry(userId, key, value)
            if (response.ok) {
                response.removed
            } else {
                throw Exception(response.error ?: "Could not delete memory entry")
            }
        }
    }
}
