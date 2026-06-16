@file:OptIn(ExperimentalMaterial3Api::class)

package com.visepanda.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.GoldLight
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.SurfaceElevated
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.VisePandaSpacing

@Composable
fun VpGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Color.Black,
            disabledContainerColor = Gold.copy(alpha = 0.4f),
            disabledContentColor = Color.Black.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = VisePandaSpacing.xl, vertical = VisePandaSpacing.md)
    ) {
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun VpSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceElevated,
            contentColor = TextPrimary,
            disabledContainerColor = SurfaceElevated.copy(alpha = 0.4f),
            disabledContentColor = TextPrimary.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = VisePandaSpacing.xl, vertical = VisePandaSpacing.md)
    ) {
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun VpGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = TextSecondary,
            disabledContentColor = TextSecondary.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
    }
}
