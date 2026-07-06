package com.visepanda.designsystem

import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

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

// ── Animation Specs ──

object VisePandaAnimation {
    val fast = tween<Float>(200)
    val normal = tween<Float>(300)
    val slow = tween<Float>(500)
    val staggerDelay = 60  // ms between staggered items
}

// ── Elevation / Shadow Specs ──

object VisePandaElevation {
    val cardDefault = 2.dp
    val cardHover = 6.dp
    val cardElevated = 8.dp
    val modal = 16.dp
    val fab = 6.dp
    val navbar = 4.dp
}

@Composable
fun VisePandaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VisePandaLightColorScheme,
        typography = VisePandaTypography,
        shapes = VisePandaShapes,
        content = content
    )
}
