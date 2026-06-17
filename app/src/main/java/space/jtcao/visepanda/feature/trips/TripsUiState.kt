package space.jtcao.visepanda.feature.trips

import space.jtcao.visepanda.domain.model.TripAsset

sealed interface TripsUiState {
    data object Loading : TripsUiState
    data class Success(val trips: List<TripAsset>) : TripsUiState
    data class Error(val message: String) : TripsUiState
}
