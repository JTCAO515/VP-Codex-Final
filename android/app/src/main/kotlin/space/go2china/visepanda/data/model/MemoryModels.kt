package space.go2china.visepanda.data.model

/**
 * Matches butler-service's `UserMemoryEntry` record
 * (butler-service/.../memory/UserMemoryEntry.java), returned by
 * GET/DELETE /butler/memory/profile.
 */
data class MemoryEntry(
    val key: String,
    val value: String,
    val confidence: Double,
    val evidence: List<String> = emptyList(),
    val source: String? = null,
    val updatedAt: String? = null,
)

data class MemoryProfileResponse(
    val ok: Boolean,
    val entries: List<MemoryEntry> = emptyList(),
    val error: String? = null,
)

data class MemoryDeleteResponse(
    val ok: Boolean,
    val removed: Boolean = false,
    val error: String? = null,
)
