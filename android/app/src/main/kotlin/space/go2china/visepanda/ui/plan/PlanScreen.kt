package space.go2china.visepanda.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import space.go2china.visepanda.data.model.TripDay
import space.go2china.visepanda.ui.components.EmptyStateView
import space.go2china.visepanda.ui.components.LoadingStateView
import space.go2china.visepanda.ui.theme.Dimens

@Composable
fun PlanScreen(
    onOpenDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.plan_title)) }) },
    ) { innerPadding ->
        when (val current = state) {
            is PlanUiState.Loading -> LoadingStateView(modifier = Modifier.padding(innerPadding))
            is PlanUiState.Empty -> EmptyStateView(modifier = Modifier.padding(innerPadding))
            is PlanUiState.Content -> PlanContent(
                state = current,
                onOpenDay = onOpenDay,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun PlanContent(
    state: PlanUiState.Content,
    onOpenDay: (Int) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.SpaceLG)) {
            Text(
                text = stringResource(R.string.plan_readiness_label),
                style = MaterialTheme.typography.labelLarge,
            )
            LinearProgressIndicator(
                progress = { state.readinessScore / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "${state.readinessScore}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Dimens.SpaceLG, vertical = Dimens.SpaceSM),
        ) {
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
