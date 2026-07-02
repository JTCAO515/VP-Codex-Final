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
 * Only [activeTripTitle] is real data (read from the same [TripRepository]
 * every other screen uses). Everything else this screen shows is honest,
 * disclosed placeholder content — there is no Supabase auth/account system
 * wired up yet. See DESIGN.md ADR-111.
 */
data class MeUiState(val activeTripTitle: String? = null)

@HiltViewModel
class MeViewModel @Inject constructor(
    tripRepository: TripRepository,
) : ViewModel() {

    val uiState: StateFlow<MeUiState> = tripRepository.observeActiveTrip()
        .map { trip -> MeUiState(activeTripTitle = trip?.summary?.title) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeUiState(),
        )
}
