package com.visepanda.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.SurfaceElevated
import com.visepanda.core.designsystem.color.TextMuted
import com.visepanda.core.designsystem.color.TextPrimary
import com.visepanda.core.designsystem.shape.VpShape

@Composable
fun VpCard(
    title: String,
    description: String = "",
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = VpShape.md,
        color = SurfaceElevated,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(VpSpacingValues.lg)) {
            if (title.isNotBlank()) {
                Text(text = title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = TextPrimary)
            }
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            content()
        }
    }
}

// Workaround: VpSpacing can't be used in top-level @Composable
internal object VpSpacingValues {
    val lg = 16.dp
}
