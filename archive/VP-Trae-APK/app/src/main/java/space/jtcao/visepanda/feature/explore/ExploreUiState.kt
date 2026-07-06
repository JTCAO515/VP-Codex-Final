package space.jtcao.visepanda.feature.explore

import space.jtcao.visepanda.domain.model.DestinationSummary

sealed interface ExploreUiState {
    data object Loading : ExploreUiState
    data class Success(val destinations: List<DestinationSummary>) : ExploreUiState
    data class Error(val message: String) : ExploreUiState
}
