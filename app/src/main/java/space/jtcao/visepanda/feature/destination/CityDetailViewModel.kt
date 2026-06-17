package space.jtcao.visepanda.feature.destination

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
import space.jtcao.visepanda.domain.usecase.GetDestinationDetailUseCase

class CityDetailViewModel(
    private val getDestinationDetail: GetDestinationDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CityDetailUiState>(CityDetailUiState.Loading)
    val uiState: StateFlow<CityDetailUiState> = _uiState.asStateFlow()

    fun load(cityId: String) {
        viewModelScope.launch {
            _uiState.value = try {
                CityDetailUiState.Success(getDestinationDetail(cityId))
            } catch (throwable: Throwable) {
                CityDetailUiState.Error(throwable.message ?: "Failed to load destination")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CityDetailViewModel(
                    getDestinationDetail = GetDestinationDetailUseCase(
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
