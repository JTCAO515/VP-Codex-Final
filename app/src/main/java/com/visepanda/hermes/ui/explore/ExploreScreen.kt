package com.visepanda.hermes.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.VisePandaShapes
import com.visepanda.designsystem.components.VpCityCard
import com.visepanda.designsystem.components.VpCityCardShimmer
import com.visepanda.designsystem.components.VpEnterAnimation
import kotlinx.coroutines.delay

// ── City data with backend images ──

data class ExploreCity(
    val name: String,
    val subtitle: String,
    val tags: List<String>,
    val imageUrl: String? = null
)

private val allCities = listOf(
    ExploreCity("Beijing", "Capital · History", listOf("Great Wall", "Forbidden City"), "https://www.go2china.space/static/img/city-beijing.jpg"),
    ExploreCity("Shanghai", "Modern · Nightlife", listOf("Bund", "Dishui Lake"), "https://www.go2china.space/static/img/city-shanghai.jpg"),
    ExploreCity("Xi'an", "Terracotta Warriors", listOf("History", "Food"), "https://www.go2china.space/static/img/city-xian.jpg"),
    ExploreCity("Chengdu", "Pandas · Sichuan", listOf("Pandas", "Hotpot"), "https://www.go2china.space/static/img/city-chengdu.jpg"),
    ExploreCity("Guangzhou", "Canton · Dim Sum", listOf("Food", "Shopping"), "https://www.go2china.space/static/img/city-guangzhou.jpg"),
    ExploreCity("Hangzhou", "West Lake · Tea", listOf("Nature", "Culture"), "https://www.go2china.space/static/img/city-hangzhou.jpg"),
    ExploreCity("Guilin", "Karst Mountains", listOf("Nature", "Scenery"), "https://www.go2china.space/static/img/city-guilin.jpg"),
    ExploreCity("Zhangjiajie", "Avatar Mountains", listOf("Nature", "Hiking"), null),
    ExploreCity("Lhasa", "Tibet · Buddhism", listOf("Culture", "History"), null),
    ExploreCity("Hong Kong", "Global · Finance", listOf("Shopping", "Food"), null)
)

// ── Explore Screen ──

@Composable
fun ExploreScreen(modifier: Modifier = Modifier) {
    var isMapView by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Simulate loading
    LaunchedEffect(Unit) {
        delay(400)
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Explore China",
                    style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${allCities.size} destinations",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            ViewToggle(
                isMap = isMapView,
                onToggle = { isMapView = !isMapView }
            )
        }

        // ── Content ──
        if (isMapView) {
            MapPlaceholder()
        } else if (isLoading) {
            ExploreShimmer()
        } else {
            CityGrid()
        }
    }
}

// ── Explore Shimmer ──

@Composable
private fun ExploreShimmer() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        itemsIndexed(listOf(1, 2, 3, 4)) { _, _ ->
            VpCityCardShimmer()
        }
    }
}

// ── View Toggle ──

@Composable
private fun ViewToggle(
    isMap: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(VisePandaShapes.small)
            .background(Surface)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(VisePandaShapes.small)
                .background(if (!isMap) Gold else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📍 Cards",
                fontSize = 13.sp,
                color = if (!isMap) Color.White else TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(VisePandaShapes.small)
                .background(if (isMap) Gold else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🗺️ Map",
                fontSize = 13.sp,
                color = if (isMap) Color.White else TextSecondary
            )
        }
    }
}

// ── City Grid (2 columns) ──

@Composable
private fun CityGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        itemsIndexed(allCities) { index, city ->
            VpEnterAnimation(index = index, staggerDelay = 50) {
                VpCityCard(
                    imageUrl = city.imageUrl,
                    cityName = city.name,
                    description = city.subtitle,
                    tags = city.tags,
                    onClick = { /* Navigate to city detail */ },
                    modifier = Modifier.fillMaxWidth(),
                    height = 190
                )
            }
        }
    }
}

// ── Map Placeholder ──

@Composable
private fun MapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(VisePandaShapes.large)
                    .background(Surface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🗺️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Interactive Map",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explore cities across China\nwith map-based navigation",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
