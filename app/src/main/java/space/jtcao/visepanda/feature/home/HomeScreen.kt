package space.jtcao.visepanda.feature.home

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
import space.jtcao.visepanda.core.designsystem.components.VpHeroCard
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader
import space.jtcao.visepanda.domain.model.DestinationSummary

@Composable
fun HomeScreen(
    onOpenExplore: () -> Unit,
    onOpenDestination: (String) -> Unit,
    onOpenChat: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenAccount: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val onFeaturedDestinationClick = featuredDestinationClickHandler(onOpenDestination)

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    when (val uiState = state) {
        HomeUiState.Loading -> {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VpHeroCard(
                    title = "Travel China Beautifully",
                    subtitle = "An elegant AI travel companion for modern explorers."
                )
                Text(
                    text = "Loading featured destinations...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                AccountEntryButton(onOpenAccount = onOpenAccount)
            }
        }

        is HomeUiState.Error -> {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VpSectionHeader(
                    title = "Featured Destinations",
                    subtitle = "Unable to load the curated list right now."
                )
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { viewModel.load() }) {
                    Text("Retry")
                }
                AccountEntryButton(onOpenAccount = onOpenAccount)
            }
        }

        is HomeUiState.Success -> {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    VpHeroCard(
                        title = "Travel China Beautifully",
                        subtitle = "An elegant AI travel companion for modern explorers."
                    )
                }
                item {
                    Button(onClick = onOpenChat) {
                        Text("Plan with AI")
                    }
                }
                item {
                    Button(onClick = onOpenTools) {
                        Text("Travel Help Center")
                    }
                }
                item {
                    AccountEntryButton(onOpenAccount = onOpenAccount)
                }
                item {
                    VpSectionHeader(
                        title = "Featured Destinations",
                        subtitle = "Curated cities for the first rewrite release."
                    )
                }
                items(
                    items = uiState.featured,
                    key = { destination -> destination.id }
                ) { destination ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VpDestinationCard(
                            title = destination.name,
                            tagline = destination.tagline,
                            onClick = { onFeaturedDestinationClick(destination) }
                        )
                        Text(
                            text = destination.vibe,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.74f)
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = onOpenExplore) {
                        Text("Open Explore")
                    }
                }
            }
        }
    }
}

internal fun featuredDestinationClickHandler(
    onOpenDestination: (String) -> Unit
): (DestinationSummary) -> Unit = { destination ->
    onOpenDestination(destination.id)
}

@Composable
private fun AccountEntryButton(onOpenAccount: () -> Unit) {
    Button(onClick = onOpenAccount) {
        Text("Login / Account")
    }
}
