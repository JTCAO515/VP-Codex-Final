package com.visepanda.feature.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.*
import com.visepanda.core.designsystem.component.VpChip
import com.visepanda.core.designsystem.spacing.VpSpacing

@Composable
fun ChatScreen() {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(VpSpacing.lg),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = "Hello! I'm Panda, your AI travel assistant.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = VpSpacing.md),
                )
            }

            // Suggestion chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VpChip(text = "3 days in Beijing", onClick = {})
                    VpChip(text = "Best cities for food", onClick = {})
                }
            }
        }

        // Input bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceElevated,
        ) {
            Row(
                modifier = Modifier.padding(VpSpacing.sm).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Panda...") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Divider,
                        cursorColor = GoldPrimary,
                    ),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = { inputText = "" },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = GoldPrimary),
                ) {
                    Text("Send", color = SurfaceDarkest)
                }
            }
        }
    }
}
