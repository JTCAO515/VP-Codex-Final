package space.go2china.visepanda.ui.explore

import space.go2china.visepanda.data.explore.ExplorePoi

sealed interface ExploreUiState {
    data object Loading : ExploreUiState
    data class Content(
        val cities: List<String>,
        val selectedCity: String,
        val attractions: List<ExplorePoi>,
        val food: List<ExplorePoi>,
        val stays: List<ExplorePoi>,
        /** Day numbers of the active trip, for the "Add to Trip" picker. Empty if no trip exists yet. */
        val availableDays: List<Int>,
        val lastAddedPoiName: String? = null,
        val isFallback: Boolean = false,
        val errorNotice: String? = null,
        val isLoading: Boolean = false,
    ) : ExploreUiState
}
