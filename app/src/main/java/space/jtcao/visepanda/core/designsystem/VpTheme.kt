package space.jtcao.visepanda.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VpColorScheme = darkColorScheme(
    primary = VpGold,
    onPrimary = VpInk,
    primaryContainer = VpClay,
    onPrimaryContainer = VpIvory,
    secondary = VpJade,
    onSecondary = VpIvory,
    background = VpInk,
    onBackground = VpIvory,
    surface = VpMist,
    onSurface = VpIvory,
    surfaceVariant = VpClay.copy(alpha = 0.28f),
    onSurfaceVariant = VpMoon,
    outline = VpGold.copy(alpha = 0.36f)
)

@Composable
fun VpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VpColorScheme,
        typography = VpTypography,
        content = content
    )
}
