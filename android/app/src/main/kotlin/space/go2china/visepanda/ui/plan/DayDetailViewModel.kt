package space.go2china.visepanda.ui.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.model.TripDay
import space.go2china.visepanda.data.repository.TripRepository
import space.go2china.visepanda.navigation.DetailDestinations

sealed interface DayDetailUiState {
    data object Loading : DayDetailUiState
    data object NotFound : DayDetailUiState
    data class Content(val day: TripDay) : DayDetailUiState
}

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val dayNumber: Int = checkNotNull(
        savedStateHandle.get<String>(DetailDestinations.DAY_NUMBER_ARG),
    ).toInt()

    val uiState: StateFlow<DayDetailUiState> = tripRepository.observeActiveTrip()
        .map { trip ->
            val day = trip?.days?.firstOrNull { it.day == dayNumber }
            if (day == null) DayDetailUiState.NotFound else DayDetailUiState.Content(day)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DayDetailUiState.Loading,
        )

    /** /goal Phase 4 — inline block-description editing. */
    fun updateBlockDescription(blockIndex: Int, newDescription: String) {
        viewModelScope.launch {
            tripRepository.updateBlockDescription(dayNumber, blockIndex, newDescription)
        }
    }

    /** /goal Phase 4 — reorder blocks within the day (move earlier/later). */
    fun moveBlockUp(blockIndex: Int) {
        if (blockIndex <= 0) return
        viewModelScope.launch { tripRepository.moveBlock(dayNumber, blockIndex, blockIndex - 1) }
    }

    fun moveBlockDown(blockIndex: Int, blockCount: Int) {
        if (blockIndex >= blockCount - 1) return
        viewModelScope.launch { tripRepository.moveBlock(dayNumber, blockIndex, blockIndex + 1) }
    }
}
