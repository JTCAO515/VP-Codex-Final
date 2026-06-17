package com.visepanda.feature.explore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.*
import com.visepanda.core.designsystem.spacing.VpSpacing

@Composable
fun ExploreScreen(
    onCityClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(VpSpacing.lg),
    ) {
        Text(
            text = "Explore Cities",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(VpSpacing.md))
        Text(
            text = "Discover China's most amazing destinations.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
        )
    }
}
