package space.go2china.visepanda.ui.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.explore.ExploreCategory
import space.go2china.visepanda.data.explore.ExploreSubcategory
import space.go2china.visepanda.data.explore.ExplorePoi
import space.go2china.visepanda.data.explore.MockUgcFeed
import space.go2china.visepanda.data.local.CityPreferences
import space.go2china.visepanda.data.model.BlockTime
import space.go2china.visepanda.data.model.TripBlock
import space.go2china.visepanda.data.repository.ExploreRepository
import space.go2china.visepanda.data.repository.LiveExploreRepository
import space.go2china.visepanda.data.repository.TripRepository
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val exploreRepository: ExploreRepository,
    private val cityPreferences: CityPreferences,
) : ViewModel() {

    private val liveRepo get() = exploreRepository as? LiveExploreRepository

    // ── internal state ──
    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    // persists current channel page pois for infinite scroll
    private val channelPois = mutableListOf<ExplorePoi>()
    private var currentPage = 1
    private var isPageLoading = false

    init {
        val savedCity = cityPreferences.getSelectedCityId()
            ?: SUPPORTED_CITIES.first().id
        showHome(savedCity)
    }

    // ─────────────────────────────────────────
    // Home
    // ─────────────────────────────────────────

    private fun showHome(cityId: String) {
        _uiState.value = ExploreUiState.Home(
            cities = SUPPORTED_CITIES,
            selectedCityId = cityId,
            ugcFeed = MockUgcFeed.forCity(cityId),
        )
    }

    fun selectCity(cityId: String) {
        cityPreferences.saveSelectedCityId(cityId)
        when (val s = _uiState.value) {
            is ExploreUiState.Home -> showHome(cityId)
            is ExploreUiState.Channel -> {
                // switch city within channel, reset pois
                channelPois.clear()
                currentPage = 1
                val newFilters = s.filters.copy(
                    subcategory = defaultSubcategory(s.category)
                )
                _uiState.value = s.copy(
                    selectedCityId = cityId,
                    pois = emptyList(),
                    filters = newFilters,
                    isLoading = true,
                    errorNotice = null,
                )
                loadChannelPage(cityId, s.category, newFilters, page = 1)
            }
            else -> showHome(cityId)
        }
    }

    fun navigateToChannel(category: ExploreCategory, locationGranted: Boolean, userLocation: String?) {
        val cityId = currentCityId()
        val defaultFilters = buildDefaultFilters(category, locationGranted, userLocation)
        channelPois.clear()
        currentPage = 1
        _uiState.value = ExploreUiState.Channel(
            category = category,
            cities = SUPPORTED_CITIES,
            selectedCityId = cityId,
            pois = emptyList(),
            filters = defaultFilters,
            isLoading = true,
            hasMore = false,
            isLive = false,
            locationGranted = locationGranted,
            locationUnavailableNotice = !locationGranted,
            errorNotice = null,
            lastAddedPoiName = null,
        )
        loadChannelPage(cityId, category, defaultFilters, page = 1)
    }

    fun navigateBack() {
        val cityId = currentCityId()
        showHome(cityId)
    }

    // ─────────────────────────────────────────
    // Filter panel interactions
    // ─────────────────────────────────────────

    fun toggleFilterPanel(panel: FilterPanelOpen) {
        updateChannel { s ->
            val newPanel = if (s.filters.panelOpen == panel) FilterPanelOpen.None else panel
            s.copy(filters = s.filters.copy(panelOpen = newPanel))
        }
    }

    fun dismissFilterPanel() {
        updateChannel { s -> s.copy(filters = s.filters.copy(panelOpen = FilterPanelOpen.None)) }
    }

    fun applyProximity(proximity: ProximityFilter) {
        updateChannel { s ->
            val newFilters = s.filters.copy(proximity = proximity, panelOpen = FilterPanelOpen.None)
            reloadChannel(s.selectedCityId, s.category, newFilters)
            s.copy(filters = newFilters, pois = emptyList(), isLoading = true, errorNotice = null)
        }
    }

    fun applySubcategory(sub: ExploreSubcategory) {
        updateChannel { s ->
            val newFilters = s.filters.copy(subcategory = sub, panelOpen = FilterPanelOpen.None)
            reloadChannel(s.selectedCityId, s.category, newFilters)
            s.copy(filters = newFilters, pois = emptyList(), isLoading = true, errorNotice = null)
        }
    }

    fun applySort(sort: SortMode) {
        updateChannel { s ->
            val newFilters = s.filters.copy(sort = sort, panelOpen = FilterPanelOpen.None)
            // Nearest requires location; if denied, keep sort as-is and ignore
            if (sort == SortMode.Nearest && !s.locationGranted) {
                return@updateChannel s.copy(filters = s.filters.copy(panelOpen = FilterPanelOpen.None))
            }
            reloadChannel(s.selectedCityId, s.category, newFilters)
            s.copy(filters = newFilters, pois = emptyList(), isLoading = true, errorNotice = null)
        }
    }

    fun applyMoreFilters(prices: Set<PriceFilter>, ratedOnly: Boolean) {
        updateChannel { s ->
            val newFilters = s.filters.copy(
                prices = prices,
                ratedOnly = ratedOnly,
                panelOpen = FilterPanelOpen.None
            )
            // "More" filters are client-side only — re-apply on existing pois
            val filtered = applyClientFilters(channelPois, newFilters)
            s.copy(filters = newFilters, pois = filtered)
        }
    }

    fun dismissLocationNotice() {
        updateChannel { s -> s.copy(locationUnavailableNotice = false) }
    }

    // ─────────────────────────────────────────
    // Infinite scroll
    // ─────────────────────────────────────────

    fun loadNextPage() {
        val s = _uiState.value as? ExploreUiState.Channel ?: return
        if (!s.hasMore || isPageLoading) return
        loadChannelPage(s.selectedCityId, s.category, s.filters, page = currentPage + 1)
    }

    // ─────────────────────────────────────────
    // Add to trip
    // ─────────────────────────────────────────

    fun addToTrip(poi: ExplorePoi, day: Int) {
        viewModelScope.launch {
            tripRepository.addPoiToDay(day, poi.toTripBlock())
            updateChannel { s -> s.copy(lastAddedPoiName = poi.name) }
        }
    }

    fun dismissAddedNotice() {
        updateChannel { s -> s.copy(lastAddedPoiName = null) }
    }

    // ─────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────

    private fun reloadChannel(cityId: String, category: ExploreCategory, filters: ChannelFilters) {
        channelPois.clear()
        currentPage = 1
        loadChannelPage(cityId, category, filters, page = 1)
    }

    private fun loadChannelPage(
        cityId: String,
        category: ExploreCategory,
        filters: ChannelFilters,
        page: Int,
    ) {
        if (isPageLoading) return
        isPageLoading = true
        viewModelScope.launch {
            val result = runCatching {
                val locationStr = filters.proximity.radiusMeters?.let { _ ->
                    currentUserLocation()
                }
                val radiusMeters = filters.proximity.radiusMeters
                val sortKey = when (filters.sort) {
                    SortMode.Nearest -> if (locationStr != null) "distance" else "weight"
                    else -> "weight"
                }
                liveRepo?.fetchChannel(
                    cityId = cityId,
                    subcategory = filters.subcategory,
                    location = locationStr,
                    radiusMeters = radiusMeters,
                    sortKey = sortKey,
                    page = page,
                ) ?: run {
                    // fallback: no live repo
                    space.go2china.visepanda.data.repository.ExplorePoiPage(
                        pois = emptyList(), hasMore = false, isLive = false
                    )
                }
            }

            result.onSuccess { pageResult ->
                val newPois = pageResult.pois
                if (page == 1) {
                    channelPois.clear()
                }
                channelPois.addAll(newPois)
                currentPage = page

                // client-side sort if needed
                if (filters.sort == SortMode.Highest) {
                    channelPois.sortByDescending { it.rating }
                } else if (filters.sort == SortMode.Cheapest) {
                    channelPois.sortWith(compareBy(nullsLast()) { if (it.costPerPerson > 0) it.costPerPerson else null })
                }

                val displayed = applyClientFilters(channelPois, filters)
                updateChannel { s ->
                    s.copy(
                        pois = displayed,
                        isLoading = false,
                        hasMore = pageResult.hasMore,
                        isLive = pageResult.isLive,
                        errorNotice = if (!pageResult.isLive && displayed.isEmpty()) "Could not load places. Check your connection." else null,
                    )
                }
            }.onFailure { err ->
                Log.e("ExploreViewModel", "fetchChannel error", err)
                updateChannel { s ->
                    s.copy(
                        isLoading = false,
                        errorNotice = "Could not load places. Check your connection.",
                    )
                }
            }
            isPageLoading = false
        }
    }

    private fun applyClientFilters(pois: List<ExplorePoi>, filters: ChannelFilters): List<ExplorePoi> {
        return pois.filter { poi ->
            val priceOk = if (filters.prices.isEmpty()) true else {
                val bucket = poi.priceBucket
                bucket != null && filters.prices.any {
                    when (it) {
                        PriceFilter.Under50  -> bucket == space.go2china.visepanda.data.explore.PriceBucket.Under50
                        PriceFilter.Under100 -> bucket == space.go2china.visepanda.data.explore.PriceBucket.Under100
                        PriceFilter.Under200 -> bucket == space.go2china.visepanda.data.explore.PriceBucket.Under200
                        PriceFilter.Above200 -> bucket == space.go2china.visepanda.data.explore.PriceBucket.Above200
                    }
                }
            }
            val ratingOk = if (!filters.ratedOnly) true else poi.hasRating
            priceOk && ratingOk
        }
    }

    /** Returns current cached user location string "lng,lat", or null if unavailable. */
    private fun currentUserLocation(): String? = _lastLocation

    private fun buildDefaultFilters(
        category: ExploreCategory,
        locationGranted: Boolean,
        userLocation: String?,
    ): ChannelFilters {
        val defaultProximity = if (locationGranted && userLocation != null) {
            ProximityFilter.Near3k
        } else {
            ProximityFilter.City
        }
        return ChannelFilters(
            proximity = defaultProximity,
            subcategory = defaultSubcategory(category),
        )
    }

    private fun defaultSubcategory(category: ExploreCategory): ExploreSubcategory = when (category) {
        ExploreCategory.Food -> ExploreSubcategory.FoodAll
        ExploreCategory.Attraction -> ExploreSubcategory.AttractionAll
        ExploreCategory.Stay -> ExploreSubcategory.StayAll
        ExploreCategory.Shopping -> ExploreSubcategory.ShoppingAll
        ExploreCategory.Experience -> ExploreSubcategory.ExperienceAll
    }

    private fun currentCityId(): String =
        when (val s = _uiState.value) {
            is ExploreUiState.Home -> s.selectedCityId
            is ExploreUiState.Channel -> s.selectedCityId
            else -> SUPPORTED_CITIES.first().id
        }

    private inline fun updateChannel(transform: (ExploreUiState.Channel) -> ExploreUiState.Channel) {
        val s = _uiState.value as? ExploreUiState.Channel ?: return
        _uiState.value = transform(s)
    }

    // ─────────────────────────────────────────
    // Location (injected from ExploreScreen after permission result)
    // ─────────────────────────────────────────

    private var _lastLocation: String? = null

    fun onLocationUpdate(lngLat: String?, granted: Boolean) {
        _lastLocation = lngLat
        updateChannel { s ->
            if (!s.locationGranted && granted) {
                // newly granted — reload with around mode if proximity != City
                val newFilters = buildDefaultFilters(s.category, granted, lngLat)
                reloadChannel(s.selectedCityId, s.category, newFilters)
                s.copy(
                    locationGranted = granted,
                    locationUnavailableNotice = false,
                    filters = newFilters,
                    pois = emptyList(),
                    isLoading = true,
                )
            } else {
                s.copy(
                    locationGranted = granted,
                    locationUnavailableNotice = !granted,
                )
            }
        }
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
        coordinates = coordinates,
    )
}

// nullsLast comparator helper
fun <T : Comparable<T>> nullsLast(): Comparator<T?> =
    compareBy(nullsLast(naturalOrder())) { it }
