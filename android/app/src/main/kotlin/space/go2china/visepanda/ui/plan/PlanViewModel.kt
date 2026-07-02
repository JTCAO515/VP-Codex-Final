package space.go2china.visepanda.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import space.go2china.visepanda.data.model.TripCompleteness
import space.go2china.visepanda.data.repository.TripRepository

@HiltViewModel
class PlanViewModel @Inject constructor(
    tripRepository: TripRepository,
) : ViewModel() {

    val uiState: StateFlow<PlanUiState> = tripRepository.observeActiveTrip()
        .map { trip ->
            if (trip == null) {
                PlanUiState.Empty
            } else {
                PlanUiState.Content(
                    trip = trip,
                    readinessScore = TripCompleteness.calculateTripCompleteness(trip).score,
                    dayCompleteness = trip.days.associate { day ->
                        day.day to TripCompleteness.calculateDayCompleteness(day)
                    },
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlanUiState.Loading,
        )
}
