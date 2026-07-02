package space.go2china.visepanda.ui.trips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.data.model.TimelineEntry
import space.go2china.visepanda.data.model.TimelinePosition
import space.go2china.visepanda.data.model.TripDay
import space.go2china.visepanda.ui.components.EmptyStateView
import space.go2china.visepanda.ui.components.LoadingStateView
import space.go2china.visepanda.ui.components.OfflineBanner
import space.go2china.visepanda.ui.components.TaxiDriverCardButton
import space.go2china.visepanda.ui.theme.Dimens

/**
 * Merges the former Today (Now/Next/Later timeline + Ask Butler entry) and
 * Plan (readiness + day-by-day list) surfaces into a single Trips
 * destination, per the v0.3.8 bottom-nav restructure — see
 * DESIGN.md ADR-110.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    onAskButler: () -> Unit,
    onOpenDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.trips_title)) }) },
    ) { innerPadding ->
        when (val current = state) {
            is TripsUiState.Loading -> LoadingStateView(modifier = Modifier.padding(innerPadding))
            is TripsUiState.Empty -> EmptyStateView(
                message = stringResource(R.string.trips_empty_message),
                modifier = Modifier.padding(innerPadding),
            )
            is TripsUiState.Content -> TripsContent(
                state = current,
                onAskButler = onAskButler,
                onOpenDay = onOpenDay,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun TripsContent(
    state: TripsUiState.Content,
    onAskButler: () -> Unit,
    onOpenDay: (Int) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        if (state.offline) {
            OfflineBanner(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimens.SpaceLG),
        ) {
            item {
                Text(
                    text = state.trip.summary.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "${state.readinessScore}% ${stringResource(R.string.plan_readiness_label).lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                LinearProgressIndicator(
                    progress = { state.readinessScore / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                OutlinedButton(onClick = onAskButler) {
                    Text(stringResource(R.string.today_ask_butler))
                }
                Spacer(modifier = Modifier.height(Dimens.SpaceLG))
            }

            if (state.timeline.isNotEmpty()) {
                items(state.timeline) { entry ->
                    TimelineEntryCard(entry)
                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                }
                item { Spacer(modifier = Modifier.height(Dimens.SpaceMD)) }
            }

            item {
                Text(
                    text = stringResource(R.string.trips_day_by_day_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            }

            items(state.trip.days) { day ->
                DayCard(
                    day = day,
                    completeness = state.dayCompleteness[day.day] ?: 0,
                    onClick = { onOpenDay(day.day) },
                )
            }
        }
    }
}

@Composable
private fun TimelineEntryCard(entry: TimelineEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Text(
                text = entry.position.label(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = entry.block.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = entry.block.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (entry.block.chineseAddress != null || entry.block.address != null) {
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                TaxiDriverCardButton(block = entry.block)
            }
        }
    }
}

private fun TimelinePosition.label(): String = when (this) {
    TimelinePosition.Now -> "NOW"
    TimelinePosition.Next -> "NEXT"
    TimelinePosition.Later -> "LATER"
}

@Composable
private fun DayCard(day: TripDay, completeness: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpaceXS)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.plan_day_label, day.day),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$completeness%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Text(
                text = day.city,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
