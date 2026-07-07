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
    MapMarker("beijing", "北京", 39.9042, 116.4074),
    MapMarker("shanghai", "上海", 31.2304, 121.4737),
    MapMarker("chengdu", "成都", 30.5728, 104.0668),
    MapMarker("guangzhou", "广州", 23.1291, 113.2644),
    MapMarker("shenzhen", "深圳", 22.5431, 114.0579),
    MapMarker("xian", "西安", 34.3416, 108.9398),
    MapMarker("guilin", "桂林", 25.2736, 110.2900),
    MapMarker("hangzhou", "杭州", 30.2741, 120.1551),
    MapMarker("chongqing", "重庆", 29.4316, 106.9123),
    MapMarker("kunming", "昆明", 25.0389, 102.7183),
    MapMarker("suzhou", "苏州", 31.2990, 120.5853),
    MapMarker("nanjing", "南京", 32.0603, 118.7969),
    MapMarker("lhasa", "拉萨", 29.6500, 91.1000),
    MapMarker("hong_kong", "香港", 22.3193, 114.1694),
    MapMarker("macau", "澳门", 22.1987, 113.5439),
    MapMarker("harbin", "哈尔滨", 45.8038, 126.5350),
    MapMarker("changsha", "长沙", 28.2282, 112.9388),
    MapMarker("wuhan", "武汉", 30.5928, 114.3055),
    MapMarker("xiamen", "厦门", 24.4798, 118.0894),
    MapMarker("qingdao", "青岛", 36.0671, 120.3826),
    MapMarker("dali", "大理", 25.5916, 100.2299),
    MapMarker("lijiang", "丽江", 26.8721, 100.2299),
    MapMarker("huangshan", "黄山", 30.1330, 118.1750),
    MapMarker("jiuzhaigou", "九寨沟", 33.2581, 103.9229),
    MapMarker("lanzhou", "兰州", 36.0611, 103.8343),
    MapMarker("hohhot", "呼和浩特", 40.8422, 111.7490),
    MapMarker("guiyang", "贵阳", 26.6470, 106.6302),
    MapMarker("fuzhou", "福州", 26.0745, 119.2965),
    MapMarker("sanya", "三亚", 18.2528, 109.5120),
    MapMarker("dunhuang", "敦煌", 40.1421, 94.6620),
    MapMarker("luoyang", "洛阳", 34.6181, 112.4540),
    MapMarker("zhangjiajie", "张家界", 29.3493, 110.4786),
    MapMarker("tibet", "西藏", 29.6500, 91.1000),
    MapMarker("yunnan", "云南", 25.0389, 102.7183),
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
