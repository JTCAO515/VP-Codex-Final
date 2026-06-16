package space.jtcao.visepanda.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.model.City
import space.jtcao.visepanda.data.repository.CityRepository

/**
 * UI state for the Home screen.
 */
sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val cities: List<City>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

/**
 * ViewModel for the Home screen.
 * Loads city data from the API on initialization.
 */
class HomeViewModel : ViewModel() {

    private val repository = CityRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCities()
    }

    /** Refresh city data (pull-to-refresh or retry) */
    fun loadCities() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val cities = repository.getCities()
                _uiState.value = HomeUiState.Success(cities)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load cities")
            }
        }
    }
}
