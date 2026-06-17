package com.visepanda.feature.trips

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.visepanda.core.designsystem.color.*
import com.visepanda.core.designsystem.component.VpPrimaryButton
import com.visepanda.core.designsystem.spacing.VpSpacing

@Composable
fun TripsScreen(
    onStartPlanning: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(VpSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No trips yet",
            style = MaterialTheme.typography.headlineMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(VpSpacing.sm))
        Text(
            text = "Start planning your China adventure!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
        )
        Spacer(modifier = Modifier.height(VpSpacing.xl))
        VpPrimaryButton(text = "Start Planning", onClick = onStartPlanning)
    }
}
