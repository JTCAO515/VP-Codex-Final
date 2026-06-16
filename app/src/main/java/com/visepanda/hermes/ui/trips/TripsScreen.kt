package com.visepanda.hermes.ui.trips

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.visepanda.designsystem.TextSecondary

@Composable
fun TripsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "My Trips",
            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            color = TextSecondary
        )
    }
}
