package space.go2china.visepanda.ui.explore

import space.go2china.visepanda.data.explore.ExploreCategory
import space.go2china.visepanda.data.explore.ExploreSubcategory
import space.go2china.visepanda.data.explore.ExplorePoi
import space.go2china.visepanda.data.explore.UgcFeedItem

/** Distance filter for "附近" tab. */
enum class ProximityFilter(val labelEn: String, val labelZh: String, val radiusMeters: Int?) {
    City("Whole City", "全城", null),
    Near500("~500m", "500m内", 500),
    Near1k("~1 km", "1km内", 1000),
    Near3k("~3 km", "3km内", 3000),
    Near5k("~5 km", "5km内", 5000),
}

/** Sort mode. */
enum class SortMode(val labelEn: String, val labelZh: String) {
    Smart("Smart", "智能排序"),
    Nearest("Nearest", "离我最近"),
    Highest("Top Rated", "评分最高"),
    Cheapest("Lowest Price", "人均最低"),
}

/** Client-side price filter. */
enum class PriceFilter(val labelEn: String, val labelZh: String) {
    Under50("Under ¥50", "¥50以下"),
    Under100("¥50–100", "¥50-100"),
    Under200("¥100–200", "¥100-200"),
    Above200("¥200+", "¥200以上"),
}

/** Which filter tab is open, if any. */
enum class FilterPanelOpen { None, Proximity, Category, Sort, More }

data class ChannelFilters(
    val proximity: ProximityFilter = ProximityFilter.City,
    val subcategory: ExploreSubcategory,   // defaults to "all" for the category
    val sort: SortMode = SortMode.Smart,
    val prices: Set<PriceFilter> = emptySet(),
    val ratedOnly: Boolean = false,
    val panelOpen: FilterPanelOpen = FilterPanelOpen.None,
)

// ───────── top-level UI state ──────────

sealed class ExploreUiState {
    /** Shown while city list is loading (fast, usually instant). */
    object Loading : ExploreUiState()

    /** Home screen state. */
    data class Home(
        val cities: List<CityItem>,
        val selectedCityId: String,
        val ugcFeed: List<UgcFeedItem>,
    ) : ExploreUiState()

    /** Channel page state (after tapping one of the 5 categories). */
    data class Channel(
        val category: ExploreCategory,
        val cities: List<CityItem>,
        val selectedCityId: String,
        val pois: List<ExplorePoi>,
        val filters: ChannelFilters,
        val isLoading: Boolean,
        val hasMore: Boolean,
        val isLive: Boolean,
        val locationGranted: Boolean,
        val locationUnavailableNotice: Boolean, // one-shot nudge
        val errorNotice: String?,
        val lastAddedPoiName: String?,
    ) : ExploreUiState()
}

data class CityItem(
    val id: String,       // matches AMAP_CITY_MAP key, lowercase, e.g. "beijing"
    val nameEn: String,
    val nameZh: String,
)

/** All 9 supported cities, ordered by tourist priority. */
val SUPPORTED_CITIES = listOf(
    CityItem("beijing",   "Beijing",    "北京"),
    CityItem("shanghai",  "Shanghai",   "上海"),
    CityItem("chengdu",   "Chengdu",    "成都"),
    CityItem("xian",      "Xi'an",      "西安"),
    CityItem("guangzhou", "Guangzhou",  "广州"),
    CityItem("hangzhou",  "Hangzhou",   "杭州"),
    CityItem("suzhou",    "Suzhou",     "苏州"),
    CityItem("chongqing", "Chongqing",  "重庆"),
    CityItem("nanjing",   "Nanjing",    "南京"),
)
