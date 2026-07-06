package space.jtcao.visepanda.feature.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.repository.TripRepository
import space.jtcao.visepanda.data.trip.TripAssetRepositoryImpl
import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.usecase.GetTripAssetsUseCase

class TripsViewModel(
    private val getTripAssets: GetTripAssetsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TripsUiState>(TripsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun load(force: Boolean = false) {
        if (observeJob != null && !force) return

        observeJob?.cancel()
        _uiState.value = TripsUiState.Loading
        observeJob = viewModelScope.launch {
            getTripAssets()
                .map { trips -> trips.sortedByDescending(TripAsset::updatedAt) }
                .catch { throwable ->
                    _uiState.value = TripsUiState.Error(
                        throwable.message ?: "Failed to load saved trips"
                    )
                }
                .collect { trips ->
                    _uiState.value = TripsUiState.Success(trips)
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                )
                val repository = TripAssetRepositoryImpl(TripRepository(application))
                TripsViewModel(
                    getTripAssets = GetTripAssetsUseCase(repository)
                )
            }
        }
    }
}
