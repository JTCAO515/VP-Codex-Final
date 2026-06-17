package space.jtcao.visepanda.domain.repository

import space.jtcao.visepanda.domain.model.ToolEntry

interface ToolRepository {
    suspend fun getEntries(): List<ToolEntry>
}
