package space.go2china.visepanda.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.explore.ExploreCategory
import space.go2china.visepanda.data.explore.ExplorePoi
import space.go2china.visepanda.data.explore.MockExploreData
import space.go2china.visepanda.data.model.BlockTime
import space.go2china.visepanda.data.model.TripBlock
import space.go2china.visepanda.data.repository.TripRepository

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val tripRepository: TripRepository,
) : ViewModel() {

    private val selectedCity = MutableStateFlow(MockExploreData.cities.first())
    private val lastAddedPoiName = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExploreUiState> = combine(
        tripRepository.observeActiveTrip(),
        selectedCity,
        lastAddedPoiName,
    ) { trip, city, lastAdded ->
        val cityPois = MockExploreData.pois.filter { it.city == city }
        ExploreUiState.Content(
            cities = MockExploreData.cities,
            selectedCity = city,
            attractions = cityPois.filter { it.category == ExploreCategory.Attraction },
            food = cityPois.filter { it.category == ExploreCategory.Food },
            stays = cityPois.filter { it.category == ExploreCategory.Stay },
            availableDays = trip?.days?.map { it.day }.orEmpty(),
            lastAddedPoiName = lastAdded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExploreUiState.Loading,
    )

    fun selectCity(city: String) {
        selectedCity.value = city
    }

    fun addToTrip(poi: ExplorePoi, day: Int) {
        viewModelScope.launch {
            tripRepository.addPoiToDay(day, poi.toTripBlock())
            lastAddedPoiName.update { poi.name }
        }
    }

    fun dismissAddedNotice() {
        lastAddedPoiName.update { null }
    }

    private fun ExplorePoi.toTripBlock(): TripBlock = TripBlock(
        time = BlockTime.Flexible,
        title = "$name ($chineseName)",
        description = description,
        sourceLabel = "Explore",
    )
}
