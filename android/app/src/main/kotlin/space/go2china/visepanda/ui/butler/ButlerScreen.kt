package space.go2china.visepanda.ui.butler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@OptIn(ExperimentalLayoutApi::class)
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
            )
            Text(
                text = "Ask for a lighter day, booking gaps, food ideas, or a practical next step. The plan updates in Today and Plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
            ) {
                ButlerUiState.DEFAULT_SUGGESTIONS.forEach { suggestion ->
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
                if (response == null) {
                    Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(text = response.headline, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                    Text(text = response.body, style = MaterialTheme.typography.bodyMedium)
                    if (response.highlights.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        response.highlights.forEach { highlight ->
                            Text(text = "- $highlight", style = MaterialTheme.typography.bodySmall)
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
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        Column(modifier = Modifier.padding(Dimens.SpaceLG)) {
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
            ) {
                state.suggestions.take(3).forEach { suggestion ->
                    AssistChip(
                        onClick = { onSuggestion(suggestion) },
                        label = { Text(suggestion) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = onInputChange,
                    enabled = !state.sending,
                    placeholder = { Text("Ask VisePanda...") },
                    minLines = 1,
                    maxLines = 4,
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
