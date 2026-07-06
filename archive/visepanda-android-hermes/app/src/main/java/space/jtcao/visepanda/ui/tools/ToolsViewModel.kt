package space.jtcao.visepanda.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import space.jtcao.visepanda.data.api.ApiConfig
import space.jtcao.visepanda.data.model.ToolItem
import space.jtcao.visepanda.data.model.ToolContent
import java.net.URL

data class ToolEntry(val name: String, val description: String)
data class ToolDetail(val name: String, val title: String, val sections: List<ToolSection>)
data class ToolSection(val title: String, val body: String, val items: List<String>)

sealed class ToolsUiState {
    data object Loading : ToolsUiState()
    data class Success(val tools: List<ToolEntry>) : ToolsUiState()
    data class Error(val message: String) : ToolsUiState()
}

class ToolsViewModel : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow<ToolsUiState>(ToolsUiState.Loading)
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ToolsUiState.Loading
            try {
                val url = URL("${ApiConfig.BASE_URL}/api/tools")
                val response = url.readText()
                val root = json.parseToJsonElement(response).jsonObject
                val toolsObj = root["tools"]?.jsonObject ?: run {
                    _uiState.value = ToolsUiState.Error("No tools found")
                    return@launch
                }
                val tools = toolsObj.entries.map { (name, element) ->
                    ToolEntry(name = name, description = element.jsonPrimitive.content)
                }
                _uiState.value = ToolsUiState.Success(tools)
            } catch (e: Exception) {
                _uiState.value = ToolsUiState.Error(e.message ?: "Failed to load tools")
            }
        }
    }
}
