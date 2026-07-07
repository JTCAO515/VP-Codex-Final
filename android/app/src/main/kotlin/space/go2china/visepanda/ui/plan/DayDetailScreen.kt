package space.go2china.visepanda.ui.plan

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.data.model.BlockTime
import space.go2china.visepanda.data.model.BookingCandidate
import space.go2china.visepanda.data.model.BookingCandidateStatus
import space.go2china.visepanda.data.model.TripBlock
import space.go2china.visepanda.data.model.TripDay
import space.go2china.visepanda.ui.components.EmptyStateView
import space.go2china.visepanda.ui.components.LoadingStateView
import space.go2china.visepanda.ui.components.TravelTalkCardButton
import space.go2china.visepanda.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    onBack: () -> Unit,
    onScheduleCandidate: (TripDay, TripBlock) -> Unit,
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
                onEditDescription = viewModel::updateBlockDescription,
                onMoveUp = viewModel::moveBlockUp,
                onMoveDown = { index -> viewModel.moveBlockDown(index, current.day.blocks.size) },
                onScheduleCandidate = onScheduleCandidate,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun DayDetailContent(
    day: TripDay,
    onEditDescription: (Int, String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onScheduleCandidate: (TripDay, TripBlock) -> Unit,
    contentPadding: PaddingValues,
) {
    var editingBlockIndex by remember { mutableIntStateOf(-1) }

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

        itemsIndexed(day.blocks) { index, block ->
            BlockDetailCard(
                block = block,
                isFirst = index == 0,
                isLast = index == day.blocks.lastIndex,
                onEditClick = { editingBlockIndex = index },
                onMoveUp = { onMoveUp(index) },
                onMoveDown = { onMoveDown(index) },
                onScheduleClick = { onScheduleCandidate(day, block) }
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
        }
    }

    if (editingBlockIndex in day.blocks.indices) {
        EditDescriptionDialog(
            initialText = day.blocks[editingBlockIndex].description,
            onDismiss = { editingBlockIndex = -1 },
            onConfirm = { newText ->
                onEditDescription(editingBlockIndex, newText)
                editingBlockIndex = -1
            },
        )
    }
}

@Composable
private fun BlockDetailCard(
    block: TripBlock,
    isFirst: Boolean,
    isLast: Boolean,
    onEditClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onScheduleClick: () -> Unit,
) {
    val isFlexible = block.time == BlockTime.Flexible

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val timeLabel = if (isFlexible) {
                    stringResource(R.string.day_detail_needs_scheduling)
                } else {
                    block.time.name
                }
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row {
                    IconButton(onClick = onMoveUp, enabled = !isFirst && !isFlexible) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move earlier")
                    }
                    IconButton(onClick = onMoveDown, enabled = !isLast && !isFlexible) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move later")
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit description")
                    }
                }
            }
            Text(text = block.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = block.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

            if (block.highlights.orEmpty().isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                block.highlights.orEmpty().forEach { highlight ->
                    Text(text = "• $highlight", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
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

            block.bookingCandidates.orEmpty().forEach { candidate -> BookingCandidateRow(candidate) }

            if (isFlexible) {
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                Text(
                    text = stringResource(R.string.day_detail_candidate_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                TextButton(
                    onClick = onScheduleClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.day_detail_ask_schedule))
                }
            }

            if (!isFlexible && (block.chineseAddress != null || block.address != null)) {
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                TravelTalkCardButton(block = block)
            }
        }
    }
}

@Composable
private fun BookingCandidateRow(candidate: BookingCandidate) {
    Column(modifier = Modifier.padding(top = Dimens.SpaceSM)) {
        Text(text = candidate.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDescriptionDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit description") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
