package com.visepanda.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.visepanda.core.designsystem.color.VisePandaColorScheme
import com.visepanda.core.designsystem.typography.VisePandaTypography

@Composable
fun VisePandaTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = VisePandaColorScheme,
        typography = VisePandaTypography,
        content = content,
    )
}
