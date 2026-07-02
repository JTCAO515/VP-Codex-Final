package space.go2china.visepanda.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Fixed light color scheme only — see Color.kt for why Dynamic Color is
 * intentionally not used. A dark theme is out of scope for v0.3.3/v0.3.4
 * (the web app does not have one either yet); this is a deliberate scope
 * cut, not an accessibility gap the team is unaware of.
 */
private val VisePandaColorScheme = lightColorScheme(
    primary = Cinnabar,
    onPrimary = PaperSoft,
    primaryContainer = PaperWarm,
    onPrimaryContainer = CinnabarDeep,
    secondary = Gold,
    onSecondary = Ink,
    tertiary = Sage,
    onTertiary = PaperSoft,
    background = Paper,
    onBackground = Ink,
    surface = PaperSoft,
    onSurface = Ink,
    surfaceVariant = PaperWarm,
    onSurfaceVariant = InkMuted,
    outline = InkSoft,
    error = CinnabarDeep,
    onError = PaperSoft,
)

/**
 * v0.3.7: medium/large bumped from 8dp/16dp to 16dp/20dp to match the
 * operator-approved Figma Make visual reference's "soft card" roundness
 * (see DESIGN.md ADR-105) — cards read noticeably softer/rounder than the
 * v0.3.3 baseline now, on purpose.
 */
private val VisePandaShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimens.RadiusSM),
    small = RoundedCornerShape(Dimens.RadiusSM),
    medium = RoundedCornerShape(Dimens.RadiusLG),
    large = RoundedCornerShape(Dimens.RadiusXL),
    extraLarge = RoundedCornerShape(Dimens.RadiusXL),
)

@Composable
fun VisePandaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VisePandaColorScheme,
        typography = VisePandaTypography,
        shapes = VisePandaShapes,
        content = content,
    )
}
