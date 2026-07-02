package space.go2china.visepanda.ui.plan

import space.go2china.visepanda.data.model.TripState

sealed interface PlanUiState {
    data object Loading : PlanUiState
    data object Empty : PlanUiState
    data class Content(
        val trip: TripState,
        val readinessScore: Int,
        val dayCompleteness: Map<Int, Int>,
    ) : PlanUiState
}
