package space.go2china.visepanda.ui.explore

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.data.explore.ExplorePoi
import space.go2china.visepanda.ui.components.LoadingStateView
import space.go2china.visepanda.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingPoi by remember { mutableStateOf<ExplorePoi?>(null) }

    val currentState = state
    if (currentState is ExploreUiState.Content) {
        val addedName = currentState.lastAddedPoiName
        LaunchedEffect(addedName) {
            if (addedName != null) {
                snackbarHostState.showSnackbar("Added \"$addedName\" to the trip")
                viewModel.dismissAddedNotice()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Explore") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { innerPadding ->
        when (currentState) {
            is ExploreUiState.Loading -> LoadingStateView(modifier = Modifier.padding(innerPadding))
            is ExploreUiState.Content -> ExploreContent(
                state = currentState,
                onSelectCity = viewModel::selectCity,
                onAddToTrip = { poi -> pendingPoi = poi },
                contentPadding = innerPadding,
            )
        }
    }

    pendingPoi?.let { poi ->
        val availableDays = (currentState as? ExploreUiState.Content)?.availableDays.orEmpty()
        AddToTripDialog(
            poi = poi,
            availableDays = availableDays,
            onDismiss = { pendingPoi = null },
            onConfirm = { day ->
                viewModel.addToTrip(poi, day)
                pendingPoi = null
            },
        )
    }
}

@Composable
private fun ExploreContent(
    state: ExploreUiState.Content,
    onSelectCity: (String) -> Unit,
    onAddToTrip: (ExplorePoi) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(
            start = Dimens.SpaceLG,
            end = Dimens.SpaceLG,
            top = Dimens.SpaceLG,
            bottom = Dimens.BottomNavContentClearance,
        ),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                state.cities.forEach { city ->
                    FilterChip(
                        selected = city == state.selectedCity,
                        onClick = { onSelectCity(city) },
                        label = { Text(city) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        }

        item {
            if (state.isFallback) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(Dimens.RadiusMD),
                    modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.SpaceLG)
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                        Text(
                            text = "Live POIs offline. Displaying local static recommendations instead.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        item { PoiSection(title = "Attractions", pois = state.attractions, onAddToTrip = onAddToTrip) }
        item { PoiSection(title = "Food", pois = state.food, onAddToTrip = onAddToTrip) }
        item { PoiSection(title = "Stay", pois = state.stays, onAddToTrip = onAddToTrip) }
    }
}

@Composable
private fun PoiSection(title: String, pois: List<ExplorePoi>, onAddToTrip: (ExplorePoi) -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
        items(pois, key = { it.id }) { poi ->
            PoiCard(poi = poi, onAddToTrip = { onAddToTrip(poi) })
        }
    }
    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
}

@Composable
private fun PoiCard(poi: ExplorePoi, onAddToTrip: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(220.dp),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Text(
                text = poi.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = poi.chineseName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            Text(
                text = "★ ${poi.rating}  ·  ${poi.priceHint}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            Text(
                text = poi.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            
            val sourceText = poi.sourceLabel ?: "Static mock"
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            Text(
                text = "Source: $sourceText",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            TextButton(onClick = onAddToTrip) {
                Text("Add to Trip")
            }
        }
    }
}

@Composable
private fun AddToTripDialog(
    poi: ExplorePoi,
    availableDays: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add \"${poi.name}\" to your trip") },
        text = {
            if (availableDays.isEmpty()) {
                Text("You don't have a trip yet. Start a conversation with the Butler first, then come back to add places here.")
            } else {
                Column {
                    Text("Choose a day:")
                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                    availableDays.forEach { day ->
                        TextButton(onClick = { onConfirm(day) }) {
                            Text("Day $day")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}
