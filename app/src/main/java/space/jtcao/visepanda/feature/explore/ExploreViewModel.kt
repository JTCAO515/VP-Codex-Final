package space.jtcao.visepanda.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.core.common.AppConfig
import space.jtcao.visepanda.data.destination.DestinationRepositoryImpl
import space.jtcao.visepanda.data.destination.mock.MockDestinationDataSource
import space.jtcao.visepanda.data.destination.remote.RemoteDestinationDataSource
import space.jtcao.visepanda.domain.usecase.GetExploreDestinationsUseCase

class ExploreViewModel(
    private val getExploreDestinations: GetExploreDestinationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = try {
                ExploreUiState.Success(getExploreDestinations())
            } catch (throwable: Throwable) {
                ExploreUiState.Error(throwable.message ?: "Failed to load explore")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ExploreViewModel(
                    getExploreDestinations = GetExploreDestinationsUseCase(
                        repository = DestinationRepositoryImpl(
                            appMode = AppConfig.appMode,
                            remote = RemoteDestinationDataSource(),
                            mock = MockDestinationDataSource()
                        )
                    )
                )
            }
        }
    }
}
