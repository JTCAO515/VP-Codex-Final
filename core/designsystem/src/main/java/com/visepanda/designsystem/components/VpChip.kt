package com.visepanda.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.JadeGreen
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.SurfaceDefault
import com.visepanda.designsystem.SurfaceElevated
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.VisePandaSpacing

enum class ChipStyle { GOLD_OUTLINE, JADE_GREEN, JADE_GREY }

@Composable
fun VpChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ChipStyle = ChipStyle.GOLD_OUTLINE,
    selected: Boolean = false
) {
    val (bg, border, textColor) = when (style) {
        ChipStyle.GOLD_OUTLINE -> when {
            selected -> Triple(Gold, Gold, Color.Black)
            else -> Triple(SurfaceDefault, Gold, Gold)
        }
        ChipStyle.JADE_GREEN -> when {
            selected -> Triple(JadeGreen, JadeGreen, Color.White)
            else -> Triple(SurfaceDefault, JadeGreen, JadeGreen)
        }
        ChipStyle.JADE_GREY -> when {
            selected -> Triple(JadeGrey, JadeGrey, Color.White)
            else -> Triple(SurfaceElevated, JadeGrey, TextSecondary)
        }
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(9999.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Text(
            text = text,
            color = textColor,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = VisePandaSpacing.lg, vertical = VisePandaSpacing.sm)
        )
    }
}
