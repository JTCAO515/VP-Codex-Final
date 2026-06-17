package space.jtcao.visepanda.feature.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpHeroCard
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun CityDetailScreen(
    cityId: String,
    onPlanTrip: (String) -> Unit,
    viewModel: CityDetailViewModel = viewModel(factory = CityDetailViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(cityId) {
        viewModel.load(cityId)
    }

    when (val uiState = state) {
        CityDetailUiState.Loading -> {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VpSectionHeader(
                    title = "Destination Detail",
                    subtitle = "Loading your curated city brief."
                )
                Text(
                    text = "Preparing destination details...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        is CityDetailUiState.Error -> {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VpSectionHeader(
                    title = "Destination Detail",
                    subtitle = "Unable to load this city right now."
                )
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { viewModel.load(cityId) }) {
                    Text("Retry")
                }
            }
        }

        is CityDetailUiState.Success -> {
            val detail = uiState.detail
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    VpHeroCard(
                        title = detail.name,
                        subtitle = detail.headline
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Best stay: ${detail.bestDays}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Budget: ${detail.budget}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                        )
                    }
                }
                item {
                    VpSectionHeader(
                        title = "Highlights",
                        subtitle = "Editorial picks for your first pass."
                    )
                }
                items(detail.highlights) { highlight ->
                    Text(
                        text = "• $highlight",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    VpSectionHeader(
                        title = "Must Eat",
                        subtitle = "Signature bites worth planning around."
                    )
                }
                items(detail.foods) { food ->
                    Text(
                        text = "• $food",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    VpSectionHeader(
                        title = "Travel Tips",
                        subtitle = "Keep the trip smooth and elegant."
                    )
                }
                items(detail.tips) { tip ->
                    Text(
                        text = "• $tip",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = { onPlanTrip(cityId) }) {
                        Text("Plan my trip")
                    }
                }
            }
        }
    }
}
