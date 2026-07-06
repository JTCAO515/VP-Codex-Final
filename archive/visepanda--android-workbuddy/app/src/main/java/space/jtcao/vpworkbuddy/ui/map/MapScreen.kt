package space.jtcao.vpworkbuddy.ui.map

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import space.jtcao.vpworkbuddy.data.model.MapMarker

/**
 * Full China map with 36 city markers using osmdroid.
 *
 * osmdroid uses OpenStreetMap data — works worldwide including China,
 * no API key required.
 */
@Composable
fun MapScreen(
    onCityClick: (String) -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCity by remember { mutableStateOf<MapMarker?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MapUiState.Loading -> { /* Map will load shortly */ }
            is MapUiState.Success -> {
                OSMChinaMap(
                    cities = state.cities,
                    onMarkerClick = { city ->
                        selectedCity = city
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // City info popup
                selectedCity?.let { city ->
                    CityInfoPopup(
                        city = city,
                        onDismiss = { selectedCity = null },
                        onViewDetail = {
                            selectedCity = null
                            onCityClick(city.name)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
            is MapUiState.Error -> {
                Text(
                    text = "Failed to load map data",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * osmdroid MapView wrapped in Compose AndroidView.
 * Center: China (35.86, 104.19) at zoom 4 with 36 city markers.
 */
@Composable
private fun OSMChinaMap(
    cities: List<MapMarker>,
    onMarkerClick: (MapMarker) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Configure osmdroid once
    remember {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = context.cacheDir
            osmdroidTileCache = context.cacheDir.resolve("tiles")
        }
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                setBuiltInZoomControls(false)

                // Center on China
                controller.setZoom(4.0)
                controller.setCenter(GeoPoint(35.86, 104.19))

                // Add city markers
                cities.forEach { city ->
                    val marker = Marker(this).apply {
                        position = GeoPoint(city.lat, city.lng)
                        title = city.name
                        snippet = city.vibe
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = createCityMarkerIcon(ctx)
                        setOnMarkerClickListener { _, _ ->
                            onMarkerClick(city)
                            true
                        }
                    }
                    overlays.add(marker)
                }

                mapView = this
                invalidate()
            }
        },
        modifier = modifier,
        update = { view ->
            // Update if needed (drag/zoom state, etc.)
        }
    )
}

/**
 * Create a simple colored circle marker for cities.
 */
private fun createCityMarkerIcon(context: android.content.Context): Drawable? {
    val size = (24 * context.resources.displayMetrics.density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE8912E.toInt() // PandaAmberDark
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
    // White border
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f - 1f, paint)
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}

/**
 * Popup card when a city marker is tapped.
 */
@Composable
private fun CityInfoPopup(
    city: MapMarker,
    onDismiss: () -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onViewDetail,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = city.nameCn,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = city.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (city.vibe.isNotEmpty()) {
                Text(
                    text = city.vibe,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (city.days.isNotEmpty()) {
                Text(
                    text = "⏱️ ${city.days}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = "Tap to explore →",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
