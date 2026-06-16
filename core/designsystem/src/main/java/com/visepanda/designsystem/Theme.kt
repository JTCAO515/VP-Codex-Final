package com.visepanda.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VisePandaLightColorScheme = lightColorScheme(
    primary = Gold,
    onPrimary = Surface,
    primaryContainer = GoldLight,
    onPrimaryContainer = TextPrimary,
    secondary = JadeGrey,
    onSecondary = Surface,
    tertiary = JadeGreen,
    onTertiary = Surface,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    outlineVariant = BorderLight,
    error = ErrorRed,
    onError = Surface,
    scrim = ScrimLight
)

@Composable
fun VisePandaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VisePandaLightColorScheme,
        typography = VisePandaTypography,
        content = content
    )
}
