package com.visepanda.hermes.ui.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.GoldLight
import com.visepanda.designsystem.GoldPale
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.SurfaceElevated
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.TextTertiary
import com.visepanda.designsystem.VisePandaShapes
import com.visepanda.designsystem.VisePandaSpacing
import com.visepanda.designsystem.components.VpCard
import com.visepanda.designsystem.components.VpCityCard
import com.visepanda.designsystem.components.VpCityCardShimmer
import com.visepanda.designsystem.components.VpEnterAnimation
import com.visepanda.designsystem.components.VpGoldButton
import com.visepanda.designsystem.components.VpShimmer
import kotlinx.coroutines.delay

// ── City data with images ──

data class CityData(
    val name: String,
    val description: String,
    val tags: List<String>,
    val imageUrl: String? = null
)

data class InspirationData(
    val icon: String,
    val title: String,
    val description: String
)

data class EssentialData(
    val icon: String,
    val label: String
)

private val featuredCities = listOf(
    CityData("Beijing", "Capital of China", listOf("Culture", "History"), "https://www.go2china.space/static/img/city-beijing.jpg"),
    CityData("Shanghai", "Pearl of the Orient", listOf("Modern", "Nightlife"), "https://www.go2china.space/static/img/city-shanghai.jpg"),
    CityData("Xi'an", "Ancient Capital", listOf("History", "Food"), "https://www.go2china.space/static/img/city-xian.jpg"),
    CityData("Chengdu", "Home of Pandas", listOf("Nature", "Food"), "https://www.go2china.space/static/img/city-chengdu.jpg"),
    CityData("Guangzhou", "Canton Cuisine", listOf("Food", "Shopping"), "https://www.go2china.space/static/img/city-guangzhou.jpg")
)

private val inspirations = listOf(
    InspirationData("🗺️", "First Time in China", "Essential tips & must-see destinations for your inaugural visit."),
    InspirationData("🍜", "Foodie's Journey", "From Beijing duck to dim sum — a culinary tour across China."),
    InspirationData("🏯", "Hidden Gems", "Lesser-known destinations that offer authentic Chinese experiences.")
)

private val essentials = listOf(
    EssentialData("📱", "Visa"),
    EssentialData("💰", "Currency"),
    EssentialData("🌐", "VPN"),
    EssentialData("🚄", "Trains"),
    EssentialData("🏨", "Hotels"),
    EssentialData("🗣️", "Language")
)

// ── Home Screen ──

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }

    // Simulate initial loading
    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    if (isLoading) {
        HomeShimmer(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .background(Background)
        ) {
            // Hero
            item {
                HeroSection()
            }

            // Featured Cities
            item {
                SectionHeader(title = "Featured Cities", actionText = "See all")
            }
            item {
                FeaturedCitiesRow()
            }

            // Inspiration
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Inspiration", actionText = null)
            }
            item {
                InspirationGrid()
            }

            // Essentials
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Travel Essentials", actionText = null)
            }
            item {
                EssentialsGrid()
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ── Shimmer Loading State ──

@Composable
private fun HomeShimmer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = VisePandaSpacing.lg)
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
            VpShimmer(widthFraction = 0.35f, height = 28)
            Spacer(modifier = Modifier.height(12.dp))
            VpShimmer(widthFraction = 0.7f, height = 16)
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            VpShimmer(widthFraction = 0.4f, height = 22)
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) {
                    VpCityCardShimmer(modifier = Modifier.width(200.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            VpShimmer(widthFraction = 0.3f, height = 22)
            Spacer(modifier = Modifier.height(12.dp))
            VpShimmer(widthFraction = 1f, height = 80)
            Spacer(modifier = Modifier.height(12.dp))
            VpShimmer(widthFraction = 1f, height = 80)
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            VpShimmer(widthFraction = 0.35f, height = 22)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) {
                    VpShimmer(widthFraction = 1f, height = 100)
                }
            }
        }
    }
}

// ── Hero Section with Background Image ──

@Composable
private fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Background image with overlay
        AsyncImage(
            model = "https://www.go2china.space/static/img/city-shanghai.jpg",
            contentDescription = "China travel hero",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Warm dark gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x662D2D2D),
                            Color(0x882D2D2D),
                            Color(0xCC2D2D2D)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldLight, Gold)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Headline
            Text(
                text = "Your AI China\nTravel Companion",
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = Color.White,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "Plan your perfect China journey with AI-powered recommendations.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ── Featured Cities ──

@Composable
private fun FeaturedCitiesRow() {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(featuredCities) { city ->
            VpEnterAnimation(index = featuredCities.indexOf(city), staggerDelay = 80) {
                VpCityCard(
                    imageUrl = city.imageUrl,
                    cityName = city.name,
                    description = city.description,
                    tags = city.tags,
                    onClick = { /* Navigate to city detail */ },
                    modifier = Modifier.width(220.dp),
                    height = 240
                )
            }
        }
    }
}

// ── Inspiration Grid ──

@Composable
private fun InspirationGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        inspirations.forEachIndexed { index, item ->
            VpEnterAnimation(index = index, staggerDelay = 80) {
                InspirationCard(item)
            }
        }
    }
}

@Composable
private fun InspirationCard(item: InspirationData) {
    VpCard(
        onClick = { /* Navigate to guide */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(VisePandaShapes.small)
                    .background(GoldPale),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}

// ── Essentials Grid ──

@Composable
private fun EssentialsGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        essentials.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    EssentialTile(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size < 3) {
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun EssentialTile(
    item: EssentialData,
    modifier: Modifier = Modifier
) {
    VpCard(
        onClick = { /* Show essential info */ },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = item.icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.label,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Section Header ──

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = "See all"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        if (actionText != null) {
            Text(
                text = actionText,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Gold
            )
        }
    }
}
