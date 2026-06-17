package space.jtcao.visepanda.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.model.City
import space.jtcao.visepanda.data.model.CityDetail
import space.jtcao.visepanda.data.repository.CityRepository

// ── City List State ──

sealed class CityListUiState {
    data object Loading : CityListUiState()
    data class Success(val cities: List<Pair<String, City>>) : CityListUiState()
    data class Error(val message: String) : CityListUiState()
}

class CityListViewModel : ViewModel() {
    private val repository = CityRepository()
    private val _uiState = MutableStateFlow<CityListUiState>(CityListUiState.Loading)
    val uiState: StateFlow<CityListUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CityListUiState.Loading
            try {
                val cities = repository.getCities()
                _uiState.value = CityListUiState.Success(cities)
            } catch (e: Exception) {
                _uiState.value = CityListUiState.Error(e.message ?: "Failed")
            }
        }
    }
}

// ── City Detail State ──

sealed class CityDetailUiState {
    data object Loading : CityDetailUiState()
    data class Success(val detail: CityDetail, val cityName: String) : CityDetailUiState()
    data class Error(val message: String) : CityDetailUiState()
}

class CityDetailViewModel : ViewModel() {
    private val repository = CityRepository()
    private val _uiState = MutableStateFlow<CityDetailUiState>(CityDetailUiState.Loading)
    val uiState: StateFlow<CityDetailUiState> = _uiState.asStateFlow()

    fun load(cityName: String) {
        viewModelScope.launch {
            _uiState.value = CityDetailUiState.Loading
            try {
                val detail = repository.getCityDetail(cityName)
                _uiState.value = CityDetailUiState.Success(detail, cityName)
            } catch (e: Exception) {
                _uiState.value = CityDetailUiState.Error(e.message ?: "Failed to load city detail")
            }
        }
    }
}
