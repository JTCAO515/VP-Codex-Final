package space.jtcao.visepanda.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader
import space.jtcao.visepanda.domain.model.ChatMessageItem

@Composable
fun ChatScreen(
    cityId: String? = null,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val activeCityId = cityId ?: state.cityId

    LaunchedEffect(cityId) {
        viewModel.setCityContext(cityId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VpSectionHeader(
            title = "AI Travel Concierge",
            subtitle = activeCityId?.let { "Planning ideas for ${it.replaceFirstChar(Char::titlecase)}" }
                ?: "Elegant planning for your next China trip"
        )

        if (state.messages.isEmpty() && !state.isStreaming) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Try a quick start",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                state.suggestions.forEach { suggestion ->
                    Button(
                        onClick = { viewModel.send(suggestion.prompt, activeCityId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(suggestion.title)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.messages, key = ChatMessageItem::id) { message ->
                MessageCard(message = message)
            }

            if (state.isStreaming) {
                item {
                    StreamingCard(
                        text = state.streamingText,
                        imageCount = state.streamingImages.size,
                        faqCount = state.streamingFaqs.size
                    )
                }
            }
        }

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = state.input,
            onValueChange = viewModel::updateInput,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isStreaming,
            label = { Text("Ask VisePanda") },
            placeholder = { Text("Plan Shanghai, foodie trip, 5 days...") }
        )

        Button(
            onClick = { viewModel.send(state.input, activeCityId) },
            enabled = !state.isStreaming && state.input.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isStreaming) "Streaming..." else "Send")
        }
    }
}

@Composable
private fun MessageCard(message: ChatMessageItem) {
    val isUser = message.role == "user"
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isUser) "You" else "VisePanda",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StreamingCard(
    text: String,
    imageCount: Int,
    faqCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "VisePanda",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = text.ifBlank { "Thinking..." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (imageCount > 0 || faqCount > 0) {
                Text(
                    text = buildString {
                        if (imageCount > 0) append("$imageCount image card(s)")
                        if (imageCount > 0 && faqCount > 0) append(" · ")
                        if (faqCount > 0) append("$faqCount faq item(s)")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
