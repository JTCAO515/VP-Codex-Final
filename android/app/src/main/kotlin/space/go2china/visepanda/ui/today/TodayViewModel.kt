package space.go2china.visepanda.ui.today

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

@HiltViewModel
class TodayViewModel @Inject constructor(
    tripRepository: TripRepository,
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = combine(
        tripRepository.observeActiveTrip(),
        tripRepository.observeOffline(),
    ) { trip, offline ->
        if (trip == null) {
            TodayUiState.Empty
        } else {
            TodayUiState.Content(
                trip = trip,
                timeline = TripTimeline.buildTimeline(trip),
                readinessScore = TripCompleteness.calculateTripCompleteness(trip).score,
                offline = offline,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState.Loading,
    )
}
