package space.go2china.visepanda.ui.trips

import space.go2china.visepanda.data.model.TimelineEntry
import space.go2china.visepanda.data.model.TripState

sealed interface TripsUiState {
    data object Loading : TripsUiState
    data object Empty : TripsUiState
    data class Content(
        val trip: TripState,
        val timeline: List<TimelineEntry>,
        val readinessScore: Int,
        val dayCompleteness: Map<Int, Int>,
        val offline: Boolean,
    ) : TripsUiState
}
