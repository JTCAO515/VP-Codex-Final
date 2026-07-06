package space.jtcao.visepanda.feature.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader
import space.jtcao.visepanda.domain.model.TripAsset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripsScreen(
    onStartPlanning: () -> Unit,
    viewModel: TripsViewModel = viewModel(factory = TripsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    when (val uiState = state) {
        TripsUiState.Loading -> TripsLoadingState()
        is TripsUiState.Error -> TripsErrorState(
            message = uiState.message,
            onRetry = { viewModel.load(force = true) }
        )
        is TripsUiState.Success -> {
            if (uiState.trips.isEmpty()) {
                EmptyTripsState(onStartPlanning = onStartPlanning)
            } else {
                TripsList(trips = uiState.trips)
            }
        }
    }
}

@Composable
private fun TripsLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VpSectionHeader(
            title = "Saved Trips",
            subtitle = "Loading your saved travel plans."
        )
        Text(
            text = "Loading trips...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TripsErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VpSectionHeader(
            title = "Saved Trips",
            subtitle = "We could not load your saved plans right now."
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyTripsState(onStartPlanning: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VpSectionHeader(
            title = "Saved Trips",
            subtitle = "Your AI-generated travel plans will appear here."
        )
        Text(
            text = "Start a chat and ask for an itinerary to create your first saved trip.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Button(onClick = onStartPlanning) {
            Text("Plan with AI")
        }
    }
}

@Composable
private fun TripsList(trips: List<TripAsset>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            VpSectionHeader(
                title = "Saved Trips",
                subtitle = "${trips.size} plan${if (trips.size == 1) "" else "s"} saved locally."
            )
        }
        items(trips, key = TripAsset::id) { trip ->
            TripCard(trip = trip)
        }
    }
}

@Composable
private fun TripCard(trip: TripAsset) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = trip.title.ifBlank { "Untitled Trip" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = buildMetadata(trip),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = trip.content,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            formatDate(trip.updatedAt).takeIf(String::isNotBlank)?.let { date ->
                Text(
                    text = "Updated $date",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
            }
        }
    }
}

private fun buildMetadata(trip: TripAsset): String = buildString {
    val city = trip.cityId.takeIf(String::isNotBlank)?.replaceFirstChar { it.uppercase() }
    if (city != null) {
        append(city)
    }
    if (trip.days > 0) {
        if (isNotEmpty()) append(" · ")
        append("${trip.days} day")
        if (trip.days != 1) append("s")
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))
}
