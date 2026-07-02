package space.go2china.visepanda.ui.plan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import space.go2china.visepanda.data.model.BookingCandidate
import space.go2china.visepanda.data.model.BookingCandidateStatus
import space.go2china.visepanda.data.model.TripBlock
import space.go2china.visepanda.data.model.TripDay
import space.go2china.visepanda.ui.components.EmptyStateView
import space.go2china.visepanda.ui.components.LoadingStateView
import space.go2china.visepanda.ui.components.TaxiDriverCardButton
import space.go2china.visepanda.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DayDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = (state as? DayDetailUiState.Content)?.day?.let {
                        stringResource(R.string.day_detail_title, it.day, it.city)
                    } ?: ""
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.day_detail_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val current = state) {
            is DayDetailUiState.Loading -> LoadingStateView(modifier = Modifier.padding(innerPadding))
            is DayDetailUiState.NotFound -> EmptyStateView(modifier = Modifier.padding(innerPadding))
            is DayDetailUiState.Content -> DayDetailContent(
                day = current.day,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun DayDetailContent(day: TripDay, contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(Dimens.SpaceLG),
    ) {
        item {
            Text(
                text = "${stringResource(R.string.day_detail_hotel)}: ${day.stay}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${stringResource(R.string.day_detail_transport)}: ${day.transport}",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (day.food.isNotEmpty()) {
                Text(
                    text = "${stringResource(R.string.day_detail_food)}: ${day.food.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        }

        items(day.blocks) { block ->
            BlockDetailCard(block)
            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
        }
    }
}

@Composable
private fun BlockDetailCard(block: TripBlock) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Text(text = block.time.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = block.title, style = MaterialTheme.typography.titleMedium)
            Text(text = block.description, style = MaterialTheme.typography.bodyMedium)

            if (block.highlights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                block.highlights.forEach { highlight ->
                    Text(text = "• $highlight", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (block.address != null || block.openingHours != null) {
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            }
            block.address?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            block.openingHours?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            block.bookingCandidates.forEach { candidate -> BookingCandidateRow(candidate) }

            if (block.chineseAddress != null || block.address != null) {
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                TaxiDriverCardButton(block = block)
            }
        }
    }
}

@Composable
private fun BookingCandidateRow(candidate: BookingCandidate) {
    Column(modifier = Modifier.padding(top = Dimens.SpaceSM)) {
        Text(text = candidate.label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = when (candidate.status) {
                BookingCandidateStatus.InfoOnly -> stringResource(R.string.day_detail_booking_readiness_info_only)
                BookingCandidateStatus.Planned -> candidate.note
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}
