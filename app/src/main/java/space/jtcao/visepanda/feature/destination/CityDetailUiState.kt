package space.jtcao.visepanda.feature.destination

import space.jtcao.visepanda.domain.model.DestinationDetail

sealed interface CityDetailUiState {
    data object Loading : CityDetailUiState
    data class Success(val detail: DestinationDetail) : CityDetailUiState
    data class Error(val message: String) : CityDetailUiState
}
