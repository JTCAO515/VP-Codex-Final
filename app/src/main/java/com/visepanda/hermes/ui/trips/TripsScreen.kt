package com.visepanda.hermes.ui.trips

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.visepanda.designsystem.components.VpGoldButton
import com.visepanda.designsystem.components.VpTripCard

// ── Mock Data ──

private val recentTrips = listOf(
    TripData("Beijing Adventure", "Beijing", "4 days", "Great Wall, Forbidden City, Temple of Heaven..."),
    TripData("Shanghai Express", "Shanghai", "3 days", "The Bund, Dishui Lake, French Concession..."),
    TripData("Xi'an Discovery", "Xi'an", "5 days", "Terracotta Warriors, Ancient City Wall..."),
)

private val savedTrips = listOf(
    TripData("Chengdu Food Tour", "Chengdu", "3 days", "Hotpot, pandas, Jinli Ancient Street..."),
    TripData("Guilin & Yangshuo", "Guilin", "4 days", "Li River cruise, karst mountains..."),
)

private data class TripData(
    val title: String,
    val city: String,
    val days: String,
    val preview: String
)

// ── Trips Screen ──

@Composable
fun TripsScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Recent", "Saved")
    val currentTrips = if (selectedTab == 0) recentTrips else savedTrips

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ── Header ──
        Text(
            text = "My Trips",
            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
        )

        // ── Tabs ──
        TripsTabBar(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // ── List or Empty State ──
        if (currentTrips.isEmpty()) {
            EmptyState(onStartPlanning = { /* Navigate to explore or chat */ })
        } else {
            TripList(trips = currentTrips)
        }
    }
}

// ── Tab Bar ──

@Composable
private fun TripsTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Gold else Color.Transparent,
                label = "tabBg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold
                        else androidx.compose.ui.text.font.FontWeight.Normal
                )
            }
        }
    }
}

// ── Trip List ──

@Composable
private fun TripList(trips: List<TripData>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        items(trips, key = { it.title }) { trip ->
            VpTripCard(
                title = trip.title,
                city = trip.city,
                days = trip.days,
                preview = trip.preview,
                onClick = { /* Open trip detail */ }
            )
        }
    }
}

// ── Empty State ──

@Composable
private fun EmptyState(
    onStartPlanning: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(GoldLight.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🗺️", fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No trips planned yet",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start planning your China adventure.\nYour itineraries will appear here.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        VpGoldButton(
            text = "Start Planning",
            onClick = onStartPlanning,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
