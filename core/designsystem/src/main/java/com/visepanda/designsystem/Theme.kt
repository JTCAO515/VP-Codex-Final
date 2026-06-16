package com.visepanda.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VisePandaDarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = SurfaceDark,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = JadeGrey,
    onSecondary = SurfaceDefault,
    tertiary = JadeGreen,
    onTertiary = SurfaceDefault,
    background = SurfaceDark,
    onBackground = TextPrimary,
    surface = SurfaceDefault,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    outlineVariant = BorderLight,
    error = ErrorRed,
    onError = SurfaceDefault,
    scrim = ScrimDark
)

@Composable
fun VisePandaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VisePandaDarkColorScheme,
        typography = VisePandaTypography,
        content = content
    )
}
