package space.go2china.visepanda.ui.butler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.ButlerMessageRole
import space.go2china.visepanda.data.model.InlineToolCard
import space.go2china.visepanda.data.model.InlineToolCardTone
import space.go2china.visepanda.ui.theme.Dimens

@Composable
fun ButlerScreen(
    modifier: Modifier = Modifier,
    viewModel: ButlerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(modifier = modifier) { innerPadding ->
        ButlerContent(
            state = state,
            onInputChange = viewModel::updateInput,
            onSend = viewModel::sendCurrentInput,
            onSuggestion = viewModel::sendSuggestion,
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun ButlerContent(
    state: ButlerUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .imePadding(),
    ) {
        ButlerHeader(state)

        if (state.sending) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(Dimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
        ) {
            if (state.messages.isEmpty()) {
                item {
                    EmptyButlerPrompt(onSuggestion = onSuggestion)
                }
            } else {
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
            }
        }

        ButlerComposer(
            state = state,
            onInputChange = onInputChange,
            onSend = onSend,
            onSuggestion = onSuggestion,
        )
    }
}

@Composable
private fun ButlerHeader(state: ButlerUiState) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.SpaceXS,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceLG)) {
            Text(
                text = "VisePanda Butler",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = if (state.offlineFallback) {
                    "Offline fallback · ${state.modelLabel}"
                } else {
                    state.modelLabel
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyButlerPrompt(onSuggestion: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceLG)) {
            Text(
                text = "Start with what you need in China.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Ask for a lighter day, booking gaps, food ideas, or a practical next step. The plan updates in Today and Plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
            // v0.3.11: a horizontally-scrollable single row instead of a
            // wrapping FlowRow — operator feedback was that suggestion chips
            // took up too much vertical space (DESIGN.md ADR-115).
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                items(ButlerUiState.DEFAULT_SUGGESTIONS) { suggestion ->
                    AssistChip(
                        onClick = { onSuggestion(suggestion) },
                        label = { Text(suggestion) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ButlerChatMessage) {
    val isUser = message.role == ButlerMessageRole.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            shape = RoundedCornerShape(Dimens.RadiusLG),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.92f),
        ) {
            Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
                val response = message.response
                val bubbleContentColor = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                if (response == null) {
                    Text(text = message.content, style = MaterialTheme.typography.bodyLarge, color = bubbleContentColor)
                } else {
                    Text(text = response.headline, style = MaterialTheme.typography.titleMedium, color = bubbleContentColor)
                    Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                    Text(text = response.body, style = MaterialTheme.typography.bodyMedium, color = bubbleContentColor)
                    if (response.highlights.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        response.highlights.forEach { highlight ->
                            Text(text = "- $highlight", style = MaterialTheme.typography.bodySmall, color = bubbleContentColor)
                        }
                    }
                    response.watchOut?.let {
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                    Text(
                        text = response.nextStep,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    response.toolCards.orEmpty().forEach { toolCard ->
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        InlineToolCardView(toolCard)
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineToolCardView(toolCard: InlineToolCard) {
    Card(
        shape = RoundedCornerShape(Dimens.RadiusMD),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Text(
                text = toolCard.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = toolCard.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (toolCard.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                toolCard.items.forEach { item ->
                    Text(
                        text = "- $item",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            // The web version's `href` deep-links straight into a Tools
            // category; native Tools is still an honest placeholder (lands
            // with the native Translator round), so this reads as plain
            // labelled text rather than a fake, non-functional button.
            Text(
                text = toolCard.nextAction,
                style = MaterialTheme.typography.labelMedium,
                color = when (toolCard.tone) {
                    InlineToolCardTone.Warning -> MaterialTheme.colorScheme.error
                    InlineToolCardTone.Success -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
            )
        }
    }
}

@Composable
private fun ButlerComposer(
    state: ButlerUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.SpaceXS,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Bottom padding clears the v0.3.10 floating overlay nav bar
        // (DESIGN.md ADR-114) — Chat is a top-level destination, so the
        // send/camera/mic controls must never render underneath it.
        Column(
            modifier = Modifier.padding(
                start = Dimens.SpaceLG,
                end = Dimens.SpaceLG,
                top = Dimens.SpaceLG,
                bottom = Dimens.BottomNavContentClearance,
            ),
        ) {
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            }
            // v0.3.11: a horizontally-scrollable single row instead of a
            // wrapping FlowRow — operator feedback was that the suggestion
            // row took up too large a share of the composer, crowding out
            // the input box (DESIGN.md ADR-115).
            if (state.suggestions.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                    items(state.suggestions.take(5)) { suggestion ->
                        AssistChip(
                            onClick = { onSuggestion(suggestion) },
                            label = { Text(suggestion) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
            ) {
                // Camera/Mic move inside the text field as leading/trailing
                // icons (still visual-only placeholders — see the point-of-use
                // permission note below) instead of separate Row siblings, so
                // the input box itself gets the row's remaining width instead
                // of splitting it three ways with two icon buttons.
                OutlinedTextField(
                    value = state.input,
                    onValueChange = onInputChange,
                    enabled = !state.sending,
                    placeholder = { Text("Ask VisePanda...") },
                    minLines = 2,
                    maxLines = 6,
                    shape = RoundedCornerShape(Dimens.RadiusXL),
                    leadingIcon = {
                        // Visual-only per the Figma reference's composer layout —
                        // disabled, not wired to the camera, because camera/
                        // microphone permissions are staged to be requested at
                        // point-of-use starting with the native Translator round,
                        // not granted speculatively here.
                        IconButton(onClick = {}, enabled = false) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera (coming with Translator)")
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {}, enabled = false) {
                            Icon(Icons.Filled.Mic, contentDescription = "Voice input (coming with Translator)")
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onSend,
                    enabled = state.input.isNotBlank() && !state.sending,
                ) {
                    Text("Send")
                }
            }
        }
    }
}
