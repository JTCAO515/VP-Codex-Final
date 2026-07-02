package space.go2china.visepanda.ui.today

import space.go2china.visepanda.data.model.TimelineEntry
import space.go2china.visepanda.data.model.TripState

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data object Empty : TodayUiState
    data class Content(
        val trip: TripState,
        val timeline: List<TimelineEntry>,
        val readinessScore: Int,
        val offline: Boolean,
    ) : TodayUiState
}
