package com.visepanda.hermes.ui.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.visepanda.designsystem.TextSecondary

@Composable
fun ExploreScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Explore Cities",
            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            color = TextSecondary
        )
    }
}
