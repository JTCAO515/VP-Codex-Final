package com.visepanda.hermes.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.GoldLight
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.TextTertiary
import com.visepanda.designsystem.components.VpCityCard
import com.visepanda.designsystem.components.VpGoldButton

// ── Mock Data ──

private val featuredCities = listOf(
    CityData("Beijing", "Capital of China", listOf("Culture", "History"), "https://www.go2china.space/static/img/city-beijing.jpg"),
    CityData("Shanghai", "Pearl of the Orient", listOf("Modern", "Nightlife"), "https://www.go2china.space/static/img/city-shanghai.jpg"),
    CityData("Xi'an", "Ancient Capital", listOf("History", "Food"), "https://www.go2china.space/static/img/city-xian.jpg"),
    CityData("Chengdu", "Home of Pandas", listOf("Nature", "Food"), "https://www.go2china.space/static/img/city-chengdu.jpg"),
    CityData("Guangzhou", "Canton Cuisine", listOf("Food", "Shopping"), "https://www.go2china.space/static/img/city-guangzhou.jpg")
)

private val inspirations = listOf(
    InspirationData("🗺️", "First Time in China", "Essential tips & must-see destinations for your inaugural visit.", "0A0A0A"),
    InspirationData("🍜", "Foodie's Journey", "From Beijing duck to dim sum — a culinary tour across China.", "0A0A0A")
)

private val essentials = listOf(
    EssentialData("📱", "Visa"),
    EssentialData("💰", "Currency"),
    EssentialData("🌐", "VPN"),
    EssentialData("🚄", "Trains"),
    EssentialData("🏨", "Hotels"),
    EssentialData("🗣️", "Language")
)

private data class CityData(
    val name: String,
    val description: String,
    val tags: List<String>,
    val imageUrl: String? = null
)

private data class InspirationData(
    val icon: String,
    val title: String,
    val description: String,
    val accent: String
)

private data class EssentialData(
    val icon: String,
    val label: String
)

// ── Home Screen ──

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Background)
    ) {
        // ── Hero Section ──
        item {
            HeroSection()
        }

        // ── Featured Cities ──
        item {
            SectionHeader(
                title = "Featured Cities",
                actionText = "See all"
            )
        }
        item {
            FeaturedCitiesRow()
        }

        // ── Inspiration ──
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Inspiration",
                actionText = null
            )
        }
        item {
            InspirationGrid()
        }

        // ── Essentials ──
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Travel Essentials",
                actionText = null
            )
        }
        item {
            EssentialsGrid()
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Hero Section ──

@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(top = 24.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand icon - gold circle with "V"
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Gold),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "V",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Headline
        Text(
            text = "Your AI China\nTravel Companion",
            style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = TextPrimary,
            lineHeight = 42.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Plan your perfect China journey with AI-powered recommendations, curated itineraries, and real-time travel insights.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // CTA
        VpGoldButton(
            text = "Plan Your Trip",
            onClick = { /* Navigate to Chat */ },
            modifier = Modifier.fillMaxWidth()
        )
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
            VpCityCard(
                imageUrl = city.imageUrl,
                cityName = city.name,
                description = city.description,
                tags = city.tags,
                onClick = { /* Navigate to city detail */ },
                modifier = Modifier.width(200.dp),
                height = 220
            )
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
        inspirations.forEach { item ->
            InspirationCard(item)
        }
    }
}

@Composable
private fun InspirationCard(item: InspirationData) {
    Card(
        onClick = { /* Navigate to guide */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldLight.copy(alpha = 0.3f)),
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
        // 3 items per row × 2 rows
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
                // Fill remaining space if row has fewer than 3 items
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
    Card(
        onClick = { /* Show essential info */ },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
