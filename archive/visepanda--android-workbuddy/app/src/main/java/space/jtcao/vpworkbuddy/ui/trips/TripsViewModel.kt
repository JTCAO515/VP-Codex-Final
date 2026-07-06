package space.jtcao.vpworkbuddy.ui.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.vpworkbuddy.data.model.Trip
import space.jtcao.vpworkbuddy.data.repository.TripRepository

sealed class TripsUiState {
    data object Loading : TripsUiState()
    data class Success(val trips: List<Trip>) : TripsUiState()
}

class TripsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TripRepository(application)

    private val _uiState = MutableStateFlow<TripsUiState>(TripsUiState.Loading)
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTrips().collect { trips ->
                _uiState.value = TripsUiState.Success(trips)
            }
        }
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            repository.deleteTrip(tripId)
        }
    }
}
