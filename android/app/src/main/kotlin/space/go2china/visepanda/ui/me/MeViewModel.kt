package space.go2china.visepanda.ui.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import space.go2china.visepanda.data.repository.TripRepository

/**
 * Only [activeTripTitle] and [hasCachedTripData] are real data (read from the
 * same [TripRepository] every other screen uses). Account profile fields
 * require Supabase auth.
 */
data class MeUiState(val activeTripTitle: String? = null, val hasCachedTripData: Boolean = false)

@HiltViewModel
class MeViewModel @Inject constructor(
    tripRepository: TripRepository,
) : ViewModel() {

    val uiState: StateFlow<MeUiState> = tripRepository.observeActiveTrip()
        .map { trip ->
            MeUiState(
                activeTripTitle = trip?.summary?.title,
                hasCachedTripData = trip?.days?.isNotEmpty() == true,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeUiState(),
        )
}
