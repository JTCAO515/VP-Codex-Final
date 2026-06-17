package com.visepanda.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.GoldDark
import com.visepanda.core.designsystem.color.GoldPrimary
import com.visepanda.core.designsystem.color.SurfaceDarkest
import com.visepanda.core.designsystem.color.TextMuted
import com.visepanda.core.designsystem.shape.VpShape

@Composable
fun VpPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp).fillMaxWidth(),
        shape = VpShape.sm,
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = SurfaceDarkest,
            disabledContainerColor = GoldDark,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Text(text = text)
    }
}

@Composable
fun VpTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text = text, color = GoldPrimary)
    }
}

@Composable
fun VpSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp).fillMaxWidth(),
        shape = VpShape.sm,
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceDarkest,
            contentColor = GoldPrimary,
            disabledContainerColor = SurfaceDarkest,
            disabledContentColor = TextMuted,
        ),
    ) {
        Text(text = text)
    }
}
