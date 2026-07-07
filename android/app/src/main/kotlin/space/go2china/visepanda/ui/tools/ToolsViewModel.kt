package space.go2china.visepanda.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.repository.ToolsRepository

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val toolsRepository: ToolsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ToolsUiState>(ToolsUiState.Loading)
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = ToolsUiState.Content(toolsRepository.getCategories())
        }
    }
}
