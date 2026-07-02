package space.go2china.visepanda.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Fixed light color scheme only — see Color.kt for why Dynamic Color is
 * intentionally not used. A dark theme is out of scope for v0.3.3/v0.3.4
 * (the web app does not have one either yet); this is a deliberate scope
 * cut, not an accessibility gap the team is unaware of.
 *
 * v0.3.8: every `ColorScheme` role is now explicitly assigned — see
 * Color.kt's doc comment and DESIGN.md ADR-112 for why leaving roles
 * unspecified (which silently falls back to Material's baseline purple
 * palette) caused `Card`-embedded text with no explicit color to render in
 * an unintended reddish tone instead of a neutral ink color.
 */
private val VisePandaColorScheme = lightColorScheme(
    primary = Cinnabar,
    onPrimary = PaperSoft,
    primaryContainer = PaperWarm,
    onPrimaryContainer = CinnabarDeep,
    inversePrimary = InversePrimaryTint,
    secondary = Gold,
    onSecondary = Ink,
    secondaryContainer = SecondaryContainerTint,
    onSecondaryContainer = InkMuted,
    tertiary = Sage,
    onTertiary = PaperSoft,
    tertiaryContainer = TertiaryContainerTint,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = Paper,
    onBackground = Ink,
    surface = PaperSoft,
    onSurface = Ink,
    surfaceVariant = PaperWarm,
    onSurfaceVariant = InkMuted,
    surfaceTint = Cinnabar,
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    outline = InkSoft,
    outlineVariant = OutlineVariantLight,
    error = CinnabarDeep,
    onError = PaperSoft,
    errorContainer = ErrorContainerTint,
    onErrorContainer = OnErrorContainerDark,
    scrim = Color.Black,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
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
