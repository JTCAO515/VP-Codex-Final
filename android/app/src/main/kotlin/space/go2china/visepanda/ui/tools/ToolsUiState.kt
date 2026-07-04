package space.go2china.visepanda.ui.tools

import space.go2china.visepanda.data.model.ToolCategory

sealed interface ToolsUiState {
    data object Loading : ToolsUiState
    data class Content(val categories: List<ToolCategory>) : ToolsUiState
}
