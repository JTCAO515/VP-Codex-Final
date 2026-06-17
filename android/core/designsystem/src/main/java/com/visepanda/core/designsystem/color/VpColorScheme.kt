package com.visepanda.core.designsystem.color

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable

@Immutable
val VisePandaColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = SurfaceDarkest,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = BambooGreen,
    onSecondary = SurfaceDarkest,
    background = SurfaceDefault,
    onBackground = TextPrimary,
    surface = SurfaceDefault,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    error = ErrorRed,
    onError = TextPrimary,
)
