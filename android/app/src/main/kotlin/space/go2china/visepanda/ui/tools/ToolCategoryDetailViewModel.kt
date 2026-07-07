package space.go2china.visepanda.ui.tools

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.model.ToolCategory
import space.go2china.visepanda.data.repository.ToolsRepository
import space.go2china.visepanda.navigation.DetailDestinations

sealed interface ToolCategoryDetailUiState {
    data object Loading : ToolCategoryDetailUiState
    data object NotFound : ToolCategoryDetailUiState
    data class Content(val category: ToolCategory) : ToolCategoryDetailUiState
}

@HiltViewModel
class ToolCategoryDetailViewModel @Inject constructor(
    toolsRepository: ToolsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val categoryId: String = checkNotNull(
        savedStateHandle.get<String>(DetailDestinations.TOOL_CATEGORY_ARG),
    )

    private val _uiState = MutableStateFlow<ToolCategoryDetailUiState>(ToolCategoryDetailUiState.Loading)
    val uiState: StateFlow<ToolCategoryDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val category = toolsRepository.getCategory(categoryId)
            _uiState.value = if (category == null) {
                ToolCategoryDetailUiState.NotFound
            } else {
                ToolCategoryDetailUiState.Content(category)
            }
        }
    }
}
