package com.visepanda.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.GoldPrimary
import com.visepanda.core.designsystem.color.TextSecondary

@Composable
fun VpSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = TextSecondary,
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = GoldPrimary,
            )
        }
    }
}
