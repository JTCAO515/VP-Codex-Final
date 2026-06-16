package com.visepanda.hermes.ui.trips

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.visepanda.designsystem.VisePandaShapes
import com.visepanda.designsystem.components.VpEnterAnimation
import com.visepanda.designsystem.components.VpGoldButton
import com.visepanda.designsystem.components.VpShimmer
import com.visepanda.designsystem.components.VpTripCard
import com.visepanda.hermes.data.TripItem
import com.visepanda.hermes.data.TripRepository
import kotlinx.coroutines.launch

// ── Trips Screen ──

@Composable
fun TripsScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var recentTrips by remember { mutableStateOf<List<TripItem>>(emptyList()) }
    var savedTrips by remember { mutableStateOf<List<TripItem>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val tabs = listOf("Recent", "Saved")
    val context = LocalContext.current
    val repo = remember { TripRepository(context) }

    // Load trips from API
    LaunchedEffect(selectedTab) {
        isLoading = true
        errorMsg = null
        val result = repo.getTrips()
        result.fold(
            onSuccess = { (recent, saved) ->
                recentTrips = recent
                savedTrips = saved
                isLoading = false
            },
            onFailure = { e ->
                errorMsg = e.message
                isLoading = false
            }
        )
    }

    val currentTrips = if (selectedTab == 0) recentTrips else savedTrips

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header
        Text(
            text = "My Trips",
            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
        )

        // Subtitle
        Text(
            text = if (errorMsg != null) "Connection error"
                   else if (currentTrips.isNotEmpty()) "${currentTrips.size} trips planned"
                   else if (!isLoading) "Plan your next adventure"
                   else "Loading...",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
        )

        // ── Tabs ──
        TripsTabBar(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = {
                selectedTab = it
                isLoading = true
            }
        )

        // ── Content ──
        if (isLoading) {
            TripsShimmer()
        } else if (currentTrips.isEmpty()) {
            EnhancedEmptyState(onStartPlanning = { /* Navigate to explore or chat */ })
        } else {
            TripList(trips = currentTrips)
        }
    }
}

// ── Shimmer Loading ──

@Composable
private fun TripsShimmer() {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        itemsIndexed(listOf(1, 2, 3)) { _, _ ->
            Column(modifier = Modifier.fillMaxWidth()) {
                VpShimmer(widthFraction = 0.5f, height = 20)
                Spacer(modifier = Modifier.height(8.dp))
                VpShimmer(widthFraction = 0.3f, height = 14)
                Spacer(modifier = Modifier.height(8.dp))
                VpShimmer(widthFraction = 0.8f, height = 14)
                Spacer(modifier = Modifier.height(16.dp))
            }
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
            .clip(VisePandaShapes.medium)
            .background(Surface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            @Suppress("UNUSED_EXPRESSION")
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Gold else Color.Transparent,
                label = "tabBg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(VisePandaShapes.medium)
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
private fun TripList(trips: List<TripItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        itemsIndexed(trips, key = { _, trip -> trip.id }) { index, trip ->
            VpEnterAnimation(index = index, staggerDelay = 60) {
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
}

// ── Enhanced Empty State ──

@Composable
private fun EnhancedEmptyState(
    onStartPlanning: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustrated icon area
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(GoldLight.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            // Inner decoration ring
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(GoldLight.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✈️", fontSize = 48.sp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "No trips planned yet",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your China adventure awaits.\nExplore destinations, get AI recommendations,\nand build your perfect itinerary.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        VpGoldButton(
            text = "Start Planning",
            onClick = onStartPlanning,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hint chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("💡 Ask AI", "🗺️ Explore", "🏨 Book").forEach { hint ->
                Box(
                    modifier = Modifier
                        .clip(VisePandaShapes.extraLarge)
                        .background(Surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = hint,
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}
