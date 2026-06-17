package space.jtcao.visepanda.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.model.MapMarker
import space.jtcao.visepanda.data.repository.MapRepository

/**
 * 36 major Chinese cities with coordinates.
 * Used as fallback when the API is unavailable.
 */
private val FALLBACK_CITIES = listOf(
    MapMarker("beijing", "北京", 39.9042, 116.4074, "Ancient capital", "3-5 days"),
    MapMarker("shanghai", "上海", 31.2304, 121.4737, "Modern metropolis", "3-4 days"),
    MapMarker("chengdu", "成都", 30.5728, 104.0668, "Panda & Sichuan food", "3-5 days"),
    MapMarker("guangzhou", "广州", 23.1291, 113.2644, "Canton cuisine", "2-3 days"),
    MapMarker("shenzhen", "深圳", 22.5431, 114.0579, "Tech hub", "2-3 days"),
    MapMarker("xian", "西安", 34.3416, 108.9398, "Terracotta Warriors", "3-4 days"),
    MapMarker("guilin", "桂林", 25.2736, 110.2900, "Karst landscapes", "3-4 days"),
    MapMarker("hangzhou", "杭州", 30.2741, 120.1551, "West Lake", "2-3 days"),
    MapMarker("chongqing", "重庆", 29.4316, 106.9123, "Mountain city", "3-4 days"),
    MapMarker("kunming", "昆明", 25.0389, 102.7183, "Spring city", "3-4 days"),
    MapMarker("suzhou", "苏州", 31.2990, 120.5853, "Classic gardens", "2-3 days"),
    MapMarker("nanjing", "南京", 32.0603, 118.7969, "Ancient capital", "2-3 days"),
    MapMarker("lhasa", "拉萨", 29.6500, 91.1000, "Tibetan culture", "4-6 days"),
    MapMarker("hong_kong", "香港", 22.3193, 114.1694, "Skyline & shopping", "3-5 days"),
    MapMarker("macau", "澳门", 22.1987, 113.5439, "Casinos & heritage", "2-3 days"),
    MapMarker("harbin", "哈尔滨", 45.8038, 126.5350, "Ice festival", "3-4 days"),
    MapMarker("changsha", "长沙", 28.2282, 112.9388, "Hunan spice", "2-3 days"),
    MapMarker("wuhan", "武汉", 30.5928, 114.3055, "Yangtze hub", "2-3 days"),
    MapMarker("xiamen", "厦门", 24.4798, 118.0894, "Coastal garden", "2-3 days"),
    MapMarker("qingdao", "青岛", 36.0671, 120.3826, "Beer & coastline", "2-3 days"),
    MapMarker("dali", "大理", 25.5916, 100.2299, "Ancient town", "3-4 days"),
    MapMarker("lijiang", "丽江", 26.8721, 100.2299, "UNESCO old town", "3-4 days"),
    MapMarker("huangshan", "黄山", 30.1330, 118.1750, "Mountain scenery", "2-3 days"),
    MapMarker("jiuzhaigou", "九寨沟", 33.2581, 103.9229, "Colorful lakes", "3-4 days"),
    MapMarker("lanzhou", "兰州", 36.0611, 103.8343, "Silk Road gateway", "2-3 days"),
    MapMarker("hohhot", "呼和浩特", 40.8422, 111.7490, "Inner Mongolia", "2-3 days"),
    MapMarker("guiyang", "贵阳", 26.6470, 106.6302, "Mountain retreat", "2-3 days"),
    MapMarker("fuzhou", "福州", 26.0745, 119.2965, "Fujian culture", "2-3 days"),
    MapMarker("sanya", "三亚", 18.2528, 109.5120, "Beach resort", "3-5 days"),
    MapMarker("dunhuang", "敦煌", 40.1421, 94.6620, "Mogao Caves", "2-3 days"),
    MapMarker("luoyang", "洛阳", 34.6181, 112.4540, "Longmen Grottoes", "2-3 days"),
    MapMarker("zhangjiajie", "张家界", 29.3493, 110.4786, "Avatar mountains", "3-4 days"),
    MapMarker("tibet", "西藏", 29.6500, 91.1000, "High plateau", "5-7 days"),
    MapMarker("yunnan", "云南", 25.0389, 102.7183, "Diverse cultures", "5-7 days"),
)

sealed class MapUiState {
    data object Loading : MapUiState()
    data class Success(val cities: List<MapMarker>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

class MapViewModel : ViewModel() {

    private val repository = MapRepository()

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            try {
                val markers = repository.getMarkers()
                _uiState.value = if (markers.isNotEmpty()) {
                    MapUiState.Success(markers)
                } else {
                    MapUiState.Success(FALLBACK_CITIES)
                }
            } catch (e: Exception) {
                // Fallback to hardcoded coordinates on API failure
                _uiState.value = MapUiState.Success(FALLBACK_CITIES)
            }
        }
    }
}
