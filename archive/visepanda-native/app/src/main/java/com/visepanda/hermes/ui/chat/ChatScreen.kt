package com.visepanda.hermes.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.BorderDefault
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.GoldLight
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.TextTertiary
import com.visepanda.designsystem.VisePandaElevation
import com.visepanda.designsystem.VisePandaShapes
import androidx.lifecycle.viewmodel.compose.viewModel

// Warm shadow colors for chat bubbles
private val ShadowBubble = Color(0x12C9A96E)

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    vm: ChatViewModel = viewModel()
) {
    val chatState by vm.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        ContextBar()

        if (chatState.messages.isEmpty() && !chatState.isLoading) {
            SuggestionChips(
                suggestions = vm.suggestions,
                onSuggestionClick = { vm.sendMessage(it) }
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(
                items = chatState.messages.reversed(),
                key = { it.id }
            ) { msg ->
                when {
                    msg.type == ChatMessage.MessageType.LOADING -> LoadingBubble()
                    msg.isUser -> UserBubble(msg.content)
                    else -> AiBubble(msg.content)
                }
            }
        }

        InputBar(
            onSend = { vm.sendMessage(it) },
            enabled = !chatState.isLoading
        )
    }
}

@Composable
private fun ContextBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🤖", fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "AI Travel Assistant",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(VisePandaShapes.extraLarge)
                .background(GoldLight.copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text("All Cities", fontSize = 12.sp, color = Gold)
        }
    }
}

@Composable
private fun SuggestionChips(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions.chunked(2)) { pair ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { text ->
                    Chip(text, onClick = { onSuggestionClick(text) })
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .clip(VisePandaShapes.extraLarge)
            .background(Color.White)
            .border(1.dp, Gold, VisePandaShapes.extraLarge as RoundedCornerShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextSecondary,
            maxLines = 2
        )
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    ambientColor = ShadowBubble,
                    spotColor = ShadowBubble
                )
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Gold)
                .padding(14.dp)
        ) {
            Text(text = text, color = Color.White, fontSize = 15.sp)
        }
    }
}

@Composable
private fun AiBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    ambientColor = ShadowBubble,
                    spotColor = ShadowBubble
                )
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Surface)
        ) {
            Box {
                // Gold left border accent
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(end = 0.dp)
                        .fillMaxWidth(0.012f)
                        .background(Gold)
                )
                Text(
                    text = text,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(VisePandaShapes.medium)
                .background(Surface)
                .padding(14.dp)
        ) {
            Text("thinking...", color = TextTertiary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun InputBar(
    onSend: (String) -> Unit,
    enabled: Boolean
) {
    var input by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(VisePandaShapes.extraLarge)
                .background(Background)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (input.isEmpty()) {
                Text("Ask about China travel...", color = TextTertiary, fontSize = 14.sp)
            }
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Gold),
                singleLine = true
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(VisePandaShapes.extraLarge)
                .background(if (enabled && input.isNotBlank()) Gold else BorderDefault)
                .clickable(enabled = enabled && input.isNotBlank()) {
                    onSend(input.trim())
                    input = ""
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u27A4",
                color = if (enabled && input.isNotBlank()) Color.White else TextTertiary,
                fontSize = 18.sp
            )
        }
    }
}
