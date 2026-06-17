package com.visepanda.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.Divider
import com.visepanda.core.designsystem.color.GoldPrimary
import com.visepanda.core.designsystem.color.SurfaceInteractive
import com.visepanda.core.designsystem.color.TextPrimary
import com.visepanda.core.designsystem.color.TextSecondary

@Composable
fun VpChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val bgColor = if (selected) GoldPrimary else SurfaceInteractive
    val textColor = if (selected) SurfaceInteractive else TextSecondary
    val borderColor = if (selected) GoldPrimary else Divider

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp)),
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
