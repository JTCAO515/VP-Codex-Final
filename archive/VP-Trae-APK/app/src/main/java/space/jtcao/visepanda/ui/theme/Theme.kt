package space.jtcao.visepanda.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PandaAmber,
    onPrimary = Color.Black,
    primaryContainer = PandaAmberDark,
    onPrimaryContainer = Color.Black,
    secondary = BambooGreen,
    onSecondary = Color.White,
    secondaryContainer = BambooGreenDark,
    onSecondaryContainer = Color.White,
    tertiary = ChinaRed,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.3f),
    error = ErrorRed,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = PandaAmberDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF0D8),
    onPrimaryContainer = Color(0xFF3A2500),
    secondary = BambooGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7FFD0),
    onSecondaryContainer = Color(0xFF002200),
    tertiary = ChinaRedDark,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.5f),
    error = ErrorRed,
    onError = Color.White,
)

@Composable
fun VisePandaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VisePandaTypography,
        content = content
    )
}
