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
import space.go2china.visepanda.data.repository.ExploreRepository
import space.go2china.visepanda.data.repository.TripRepository

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val selectedCity = MutableStateFlow(MockExploreData.cities.first())
    private val lastAddedPoiName = MutableStateFlow<String?>(null)
    private val explorePoisState = MutableStateFlow<Map<ExploreCategory, List<ExplorePoi>>>(emptyMap())
    private val loadingState = MutableStateFlow(false)
    private val isFallbackMode = MutableStateFlow(false)
    private val errorNotice = MutableStateFlow<String?>(null)

    init {
        // Observe selectedCity changes and fetch POIs asynchronously
        viewModelScope.launch {
            selectedCity.collect { city ->
                loadingState.value = true
                runCatching {
                    val attractions = exploreRepository.getPois(city, ExploreCategory.Attraction)
                    val food = exploreRepository.getPois(city, ExploreCategory.Food)
                    val stays = exploreRepository.getPois(city, ExploreCategory.Stay)
                    val isLive = exploreRepository.isLiveMode(city)

                    isFallbackMode.value = !isLive
                    errorNotice.value = null

                    mapOf(
                        ExploreCategory.Attraction to attractions,
                        ExploreCategory.Food to food,
                        ExploreCategory.Stay to stays
                    )
                }.onSuccess { data ->
                    explorePoisState.value = data
                    loadingState.value = false
                }.onFailure { err ->
                    // Network or API failure fallback to static mock lists
                    isFallbackMode.value = true
                    errorNotice.value = err.message ?: "POI service offline."
                    
                    val fallbackList = MockExploreData.pois.filter { it.city.equals(city, ignoreCase = true) }
                    explorePoisState.value = mapOf(
                        ExploreCategory.Attraction to fallbackList.filter { it.category == ExploreCategory.Attraction },
                        ExploreCategory.Food to fallbackList.filter { it.category == ExploreCategory.Food },
                        ExploreCategory.Stay to fallbackList.filter { it.category == ExploreCategory.Stay }
                    )
                    loadingState.value = false
                }
            }
        }
    }

    val uiState: StateFlow<ExploreUiState> = combine(
        tripRepository.observeActiveTrip(),
        selectedCity,
        lastAddedPoiName,
        explorePoisState,
        loadingState,
        isFallbackMode,
        errorNotice
    ) { args ->
        val trip = args[0] as space.go2china.visepanda.data.model.TripState?
        val city = args[1] as String
        val lastAdded = args[2] as String?
        @Suppress("UNCHECKED_CAST")
        val poisMap = args[3] as Map<ExploreCategory, List<ExplorePoi>>
        val isLoading = args[4] as Boolean
        val isFallback = args[5] as Boolean
        val err = args[6] as String?

        if (isLoading && poisMap.isEmpty()) {
            ExploreUiState.Loading
        } else {
            ExploreUiState.Content(
                cities = MockExploreData.cities,
                selectedCity = city,
                attractions = poisMap[ExploreCategory.Attraction].orEmpty(),
                food = poisMap[ExploreCategory.Food].orEmpty(),
                stays = poisMap[ExploreCategory.Stay].orEmpty(),
                availableDays = trip?.days?.map { it.day }.orEmpty(),
                lastAddedPoiName = lastAdded,
                isFallback = isFallback,
                errorNotice = err,
                isLoading = isLoading
            )
        }
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
        address = address,
        phone = phone,
        openingHours = openingHours,
        mapUrl = mapUrl,
        bookingCandidates = bookingCandidates,
        sourceLabel = sourceLabel ?: "Explore",
        coordinates = coordinates
    )
}
