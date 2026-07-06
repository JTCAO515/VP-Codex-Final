package space.jtcao.visepanda.feature.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.tools.ToolRepositoryImpl
import space.jtcao.visepanda.domain.usecase.GetToolEntriesUseCase

class ToolsViewModel(
    private val getToolEntries: GetToolEntriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ToolsUiState>(ToolsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = ToolsUiState.Loading
            _uiState.value = runCatching {
                ToolsUiState.Success(getToolEntries())
            }.getOrElse { throwable ->
                ToolsUiState.Error(throwable.message ?: "Failed to load travel help center")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ToolsViewModel(
                    getToolEntries = GetToolEntriesUseCase(
                        repository = ToolRepositoryImpl()
                    )
                )
            }
        }
    }
}
