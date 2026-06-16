package com.visepanda.hermes.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.visepanda.designsystem.BorderDefault
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.GoldLight
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.TextTertiary
import com.visepanda.designsystem.components.VpCityCard

// ── Sample Cities ──

private val allCities = listOf(
    ExploreCity("Beijing", "Capital · History", listOf("Great Wall", "Forbidden City")),
    ExploreCity("Shanghai", "Modern · Nightlife", listOf("Bund", "Dishui Lake")),
    ExploreCity("Xi'an", "Terracotta Warriors", listOf("History", "Food")),
    ExploreCity("Chengdu", "Pandas · Sichuan", listOf("Pandas", "Hotpot")),
    ExploreCity("Guangzhou", "Canton · Dim Sum", listOf("Food", "Shopping")),
    ExploreCity("Hangzhou", "West Lake · Tea", listOf("Nature", "Culture")),
    ExploreCity("Guilin", "Karst Mountains", listOf("Nature", "Scenery")),
    ExploreCity("Zhangjiajie", "Avatar Mountains", listOf("Nature", "Hiking")),
    ExploreCity("Lhasa", "Tibet · Buddhism", listOf("Culture", "History")),
    ExploreCity("Hong Kong", "Global · Finance", listOf("Shopping", "Food"))
)

private data class ExploreCity(
    val name: String,
    val subtitle: String,
    val tags: List<String>
)

// ── Explore Screen ──

@Composable
fun ExploreScreen(modifier: Modifier = Modifier) {
    var isMapView by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ── Top: Title + Toggle ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Explore China",
                style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                color = TextPrimary
            )

            // Cards / Map toggle
            ViewToggle(
                isMap = isMapView,
                onToggle = { isMapView = !isMapView }
            )
        }

        // ── Content ──
        if (isMapView) {
            MapPlaceholder()
        } else {
            CityGrid()
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
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cards option
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
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

        // Map option
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        items(allCities) { city ->
            VpCityCard(
                imageUrl = null,
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
            // Map illustration area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🗺️",
                        fontSize = 48.sp
                    )
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
