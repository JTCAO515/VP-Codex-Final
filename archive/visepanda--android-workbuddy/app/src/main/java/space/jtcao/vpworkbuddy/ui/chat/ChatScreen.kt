package space.jtcao.vpworkbuddy.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import space.jtcao.vpworkbuddy.data.model.ChatFaq
import space.jtcao.vpworkbuddy.data.model.ChatImage
import space.jtcao.vpworkbuddy.data.model.ChatMessage
import space.jtcao.vpworkbuddy.ui.components.MarkdownText

/**
 * Welcome message shown when the chat is empty.
 */
private val WELCOME_MESSAGE = "Hi! I'm your China travel assistant. Tell me what kind of trip you're planning — cities, duration, interests, budget... I'll create a personalized itinerary for you! 🌏"

/**
 * Suggestion chips for new users.
 */
private val SUGGESTIONS = listOf(
    "3 days in Beijing",
    "Shanghai food tour",
    "Chengdu panda trip",
    "Guilin nature escape",
    "Xi'an history guide",
    "Budget tips for China"
)

@Composable
fun ChatScreen(
    city: String?,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    val itemCount = uiState.messages.size + if (uiState.isStreaming || uiState.messages.isEmpty()) 1 else 0
    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
    ) {
        // ── Message List ──
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Welcome message
            if (uiState.messages.isEmpty() && !uiState.isStreaming) {
                item {
                    WelcomeBanner(city = city)
                }
                item {
                    SuggestionChips(
                        suggestions = SUGGESTIONS,
                        onChipClick = { text ->
                            viewModel.sendMessage(text, city)
                        }
                    )
                }
            }

            // Messages
            items(uiState.messages, key = { "${it.role}_${it.timestamp}" }) { msg ->
                MessageBubble(message = msg)
            }

            // Streaming message
            if (uiState.isStreaming && uiState.currentStreamText.isNotEmpty()) {
                item {
                    StreamBubble(
                        text = uiState.currentStreamText,
                        images = uiState.currentImages,
                        faqs = uiState.currentFaqs
                    )
                }
            }

            // Loading indicator at the bottom
            if (uiState.isStreaming && uiState.currentStreamText.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐼 thinking...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Error Banner ──
        uiState.error?.let { error ->
            Text(
                text = "⚠️ $error",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // ── Input Bar ──
        ChatInputBar(
            isStreaming = uiState.isStreaming,
            onSend = { text -> viewModel.sendMessage(text, city) },
            onStop = { viewModel.stopStreaming() }
        )
    }
}

// ═════════════════════════════════════════════════
// Sub-components
// ═════════════════════════════════════════════════

@Composable
private fun WelcomeBanner(city: String?) {
    val title = if (city != null) "Plan a trip to $city" else "AI Travel Assistant"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🐼",
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Powered by DeepSeek AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        MarkdownText(
            text = WELCOME_MESSAGE,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        )
    }
}

@Composable
private fun SuggestionChips(
    suggestions: List<String>,
    onChipClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Try asking:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            suggestions.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { suggestion ->
                        Button(
                            onClick = { onChipClick(suggestion) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Avatar (assistant only)
        if (!isUser) {
            Text(
                text = "🐼",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp, end = 8.dp)
            )
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            // Role label
            Text(
                text = if (isUser) "You" else "VisePanda",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUser) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp, start = if (isUser) 0.dp else 4.dp, end = if (isUser) 4.dp else 0.dp)
            )

            // Message content
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                MarkdownText(
                    text = message.content,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Images
            message.images.forEach { image ->
                Card(
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column {
                        if (image.url.isNotBlank()) {
                            AsyncImage(
                                model = image.url,
                                contentDescription = image.label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            )
                        }
                        if (image.label.isNotBlank()) {
                            Text(
                                text = "🖼️ ${image.label}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            // FAQ chips (assistant only)
            if (!isUser && message.faqs.isNotEmpty()) {
                FaqChips(faqs = message.faqs)
            }

            // Timestamp
            Text(
                text = formatTime(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun StreamBubble(
    text: String,
    images: List<ChatImage>,
    faqs: List<ChatFaq>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "🐼",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp)
        )

        Column(
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = "VisePanda",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                MarkdownText(
                    text = text,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Images
            images.forEach { image ->
                Card(
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column {
                        if (image.url.isNotBlank()) {
                            AsyncImage(
                                model = image.url,
                                contentDescription = image.label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            )
                        }
                        if (image.label.isNotBlank()) {
                            Text(
                                text = "🖼️ ${image.label}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            // FAQ chips
            if (faqs.isNotEmpty()) {
                FaqChips(faqs = faqs)
            }

            // Blinking cursor
            Text(
                text = "▊",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun FaqChips(faqs: List<ChatFaq>) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        faqs.forEach { faq ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "${faq.icon} ${faq.title}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    isStreaming: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    "Ask me about China travel...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotBlank() && !isStreaming) {
                        onSend(text.trim())
                        text = ""
                    }
                }
            ),
            singleLine = true,
            enabled = !isStreaming
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send/Stop button
        AnimatedVisibility(
            visible = isStreaming,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
            }
        }

        AnimatedVisibility(
            visible = !isStreaming && text.isNotBlank(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = {
                    onSend(text.trim())
                    text = ""
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

// ── Utility ──

private fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
