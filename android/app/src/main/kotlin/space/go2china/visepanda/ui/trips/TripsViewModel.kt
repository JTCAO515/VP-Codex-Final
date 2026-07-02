package space.go2china.visepanda.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import space.go2china.visepanda.data.model.TripCompleteness
import space.go2china.visepanda.data.model.TripTimeline
import space.go2china.visepanda.data.repository.TripRepository

/**
 * Backs the merged Trips surface (former Today + Plan) — see
 * DESIGN.md ADR-110 for why these two were combined into one destination
 * in the v0.3.8 nav restructure.
 */
@HiltViewModel
class TripsViewModel @Inject constructor(
    tripRepository: TripRepository,
) : ViewModel() {

    val uiState: StateFlow<TripsUiState> = combine(
        tripRepository.observeActiveTrip(),
        tripRepository.observeOffline(),
    ) { trip, offline ->
        if (trip == null) {
            TripsUiState.Empty
        } else {
            TripsUiState.Content(
                trip = trip,
                timeline = TripTimeline.buildTimeline(trip),
                readinessScore = TripCompleteness.calculateTripCompleteness(trip).score,
                dayCompleteness = trip.days.associate { day ->
                    day.day to TripCompleteness.calculateDayCompleteness(day)
                },
                offline = offline,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TripsUiState.Loading,
    )
}
