package com.visepanda.hermes.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.visepanda.designsystem.TextSecondary

@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AI Chat",
            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            color = TextSecondary
        )
    }
}
