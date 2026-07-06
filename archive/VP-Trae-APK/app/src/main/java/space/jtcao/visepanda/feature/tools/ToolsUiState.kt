package space.jtcao.visepanda.feature.tools

import space.jtcao.visepanda.domain.model.ToolEntry

sealed interface ToolsUiState {
    data object Loading : ToolsUiState
    data class Success(val entries: List<ToolEntry>) : ToolsUiState
    data class Error(val message: String) : ToolsUiState
}
