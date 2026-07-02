package space.go2china.visepanda.ui.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.data.model.TimelineEntry
import space.go2china.visepanda.data.model.TimelinePosition
import space.go2china.visepanda.ui.components.EmptyStateView
import space.go2china.visepanda.ui.components.LoadingStateView
import space.go2china.visepanda.ui.components.OfflineBanner
import space.go2china.visepanda.ui.components.TaxiDriverCardButton
import space.go2china.visepanda.ui.theme.Dimens

@Composable
fun TodayScreen(
    onAskButler: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(modifier = modifier) { innerPadding ->
        when (val current = state) {
            is TodayUiState.Loading -> LoadingStateView(modifier = Modifier.padding(innerPadding))
            is TodayUiState.Empty -> EmptyStateView(
                message = "Start a conversation with the Butler to build your first trip.",
                modifier = Modifier.padding(innerPadding),
            )
            is TodayUiState.Content -> TodayContent(
                state = current,
                onAskButler = onAskButler,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun TodayContent(
    state: TodayUiState.Content,
    onAskButler: () -> Unit,
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
