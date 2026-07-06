package space.jtcao.visepanda.feature.home

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
import space.jtcao.visepanda.domain.usecase.GetFeaturedDestinationsUseCase

class HomeViewModel(
    private val getFeaturedDestinations: GetFeaturedDestinationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = try {
                HomeUiState.Success(getFeaturedDestinations())
            } catch (throwable: Throwable) {
                HomeUiState.Error(throwable.message ?: "Failed to load home")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    getFeaturedDestinations = GetFeaturedDestinationsUseCase(
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
