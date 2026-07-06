package space.jtcao.visepanda.feature.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import space.jtcao.visepanda.core.designsystem.components.VpDestinationCard
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun ExploreScreen(
    onOpenDestination: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    when (val uiState = state) {
        ExploreUiState.Loading -> {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VpSectionHeader(
                    title = "Explore China",
                    subtitle = "Loading curated cities for your next route."
                )
                Text(
                    text = "Preparing the destination list...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        is ExploreUiState.Error -> {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VpSectionHeader(
                    title = "Explore China",
                    subtitle = "Unable to load destinations right now."
                )
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { viewModel.load() }) {
                    Text("Retry")
                }
            }
        }

        is ExploreUiState.Success -> {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    VpSectionHeader(
                        title = "Explore China",
                        subtitle = "Browse cities and move into detail planning."
                    )
                }
                items(
                    items = uiState.destinations,
                    key = { destination -> destination.id }
                ) { destination ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VpDestinationCard(
                            title = destination.name,
                            tagline = destination.tagline,
                            onClick = { onOpenDestination(destination.id) }
                        )
                        Text(
                            text = destination.vibe,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.74f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
