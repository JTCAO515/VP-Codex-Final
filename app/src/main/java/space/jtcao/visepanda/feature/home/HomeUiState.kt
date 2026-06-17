package space.jtcao.visepanda.feature.home

import space.jtcao.visepanda.domain.model.DestinationSummary

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val featured: List<DestinationSummary>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
