package space.jtcao.vpworkbuddy.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.vpworkbuddy.data.repository.ToolsRepository

data class ToolEntry(val name: String, val description: String)
data class ToolDetail(val name: String, val title: String, val sections: List<ToolSection>)
data class ToolSection(val title: String, val body: String, val items: List<String>)

sealed class ToolsUiState {
    data object Loading : ToolsUiState()
    data class Success(val tools: List<ToolEntry>) : ToolsUiState()
    data class Error(val message: String) : ToolsUiState()
}

class ToolsViewModel : ViewModel() {

    private val repository = ToolsRepository()

    private val _uiState = MutableStateFlow<ToolsUiState>(ToolsUiState.Loading)
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ToolsUiState.Loading
            try {
                val tools = repository.getTools()
                val entries = tools.entries.map { (name, desc) ->
                    ToolEntry(name = name, description = desc)
                }
                _uiState.value = ToolsUiState.Success(entries)
            } catch (e: Exception) {
                _uiState.value = ToolsUiState.Error(e.message ?: "Failed to load tools")
            }
        }
    }
}
