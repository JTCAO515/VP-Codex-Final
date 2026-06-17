package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.ToolEntry
import space.jtcao.visepanda.domain.repository.ToolRepository

class GetToolEntriesUseCase(
    private val repository: ToolRepository
) {
    suspend operator fun invoke(): List<ToolEntry> = repository.getEntries()
}
