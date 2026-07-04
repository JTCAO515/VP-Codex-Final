package space.go2china.visepanda.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.explore.*
import space.go2china.visepanda.data.repository.ExploreRepository
import space.go2china.visepanda.data.repository.TripRepository
import javax.inject.Inject

data class ExploreUiState(
    val activeCityId: String = "beijing",
    val cities: List<ExploreCity> = emptyList(),
    val attractions: List<ExploreAttraction> = emptyList(),
    val foodSpots: List<ExploreFoodSpot> = emptyList(),
    val stays: List<ExploreStay> = emptyList(),
    val providerStatus: ExploreProviderStatus? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val gson = com.google.gson.Gson()

    init {
        loadStatusAndCities()
    }

    private fun loadStatusAndCities() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            runCatching {
                val status = exploreRepository.getProviderStatus()
                val citiesList = exploreRepository.listCities()
                val activeCity = _uiState.value.activeCityId
                _uiState.update {
                    it.copy(
                        providerStatus = status,
                        cities = citiesList,
                        loading = false
                    )
                }
                loadCityPois(activeCity)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = error.message ?: "Failed to fetch status and cities"
                    )
                }
            }
        }
    }

    fun selectCity(cityId: String) {
        if (_uiState.value.activeCityId == cityId) return
        _uiState.update { it.copy(activeCityId = cityId, loading = true) }
        viewModelScope.launch {
            loadCityPois(cityId)
        }
    }

    private suspend fun loadCityPois(cityId: String) {
        runCatching {
            val attractionsList = exploreRepository.listAttractions(cityId)
            val foodSpotsList = exploreRepository.listFoodSpots(cityId)
            val staysList = exploreRepository.listStays(cityId)
            _uiState.update {
                it.copy(
                    attractions = attractionsList,
                    foodSpots = foodSpotsList,
                    stays = staysList,
                    loading = false
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    loading = false,
                    errorMessage = error.message ?: "Failed to fetch POIs for $cityId"
                )
            }
        }
    }

    fun addToTrip(
        item: ExploreRichMeta,
        cityName: String,
        name: String,
        category: String,
        id: String,
        context: String?,
        onNavigateToChat: () -> Unit
    ) {
        viewModelScope.launch {
            val payload = buildExploreAddToTripPayload(item, cityName, name, category, id, context)
            val detail = if (!context.isNullOrBlank()) " (${context})" else ""
            val message = "Add ${name}${detail} in ${cityName} to my trip and ask VisePanda to rebalance the route around it."
            val payloadJson = gson.toJson(payload)

            tripRepository.setPendingExplorePoi(message, payloadJson)
            // 直接在 Explore 侧默默发送 Butler 消息以驱动 Room 缓存和网络 CanvasPatch 的应用
            runCatching { tripRepository.sendButlerMessage(message) }
            onNavigateToChat()
        }
    }

    fun saveForLater(
        item: ExploreRichMeta,
        cityName: String,
        name: String,
        category: String,
        id: String,
        context: String?,
        onNavigateToChat: () -> Unit
    ) {
        viewModelScope.launch {
            val payload = buildExploreAddToTripPayload(item, cityName, name, category, id, context)
            val message = "Save ${name} for later in ${cityName}."
            val payloadJson = gson.toJson(payload)

            tripRepository.setPendingExplorePoi(message, payloadJson)
            // 直接以 time: "Flexible" 草稿块的离线形式存入
            runCatching { tripRepository.sendButlerMessage(message) }
            onNavigateToChat()
        }
    }

    private fun buildExploreAddToTripPayload(
        item: ExploreRichMeta,
        cityName: String,
        name: String,
        category: String, // "attraction" | "food" | "stay"
        id: String,
        context: String?
    ): ExploreAddToTripPayload {
        val provider = item.sourceLabel ?: "Explore"
        val bookingKind = when (category) {
            "food" -> space.go2china.visepanda.data.model.BookingCandidateKind.Restaurant
            "stay" -> space.go2china.visepanda.data.model.BookingCandidateKind.Hotel
            else -> space.go2china.visepanda.data.model.BookingCandidateKind.Ticket
        }
        val bookingLabel = when (category) {
            "food" -> "Restaurant planning reference"
            "stay" -> "Hotel area planning reference"
            else -> "Ticket planning reference"
        }
        val mapUrl = item.location?.let {
            "https://uri.amap.com/marker?position=${it.lng},${it.lat}&name=${name}"
        } ?: "https://ditu.amap.com/search?query=${cityName} ${name}"

        return ExploreAddToTripPayload(
            id = id,
            name = name,
            cityId = _uiState.value.activeCityId,
            cityName = cityName,
            category = category,
            context = context,
            address = item.businessArea ?: cityName,
            phone = item.tel,
            openingHours = item.openHours,
            mapUrl = mapUrl,
            sourceLabel = provider,
            coordinates = item.location,
            bookingCandidates = listOf(
                space.go2china.visepanda.data.model.BookingCandidate(
                    id = "${id}-${bookingKind.name.lowercase()}",
                    kind = bookingKind,
                    label = bookingLabel,
                    provider = provider,
                    status = space.go2china.visepanda.data.model.BookingCandidateStatus.InfoOnly,
                    note = "Added from Explore for planning context only; availability, inventory, payment, and checkout are not verified yet.",
                    priceHint = item.pricePerPerson?.let { "Approx. ¥${it}/person" }
                )
            )
        )
    }
}
