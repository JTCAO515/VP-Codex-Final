package space.go2china.visepanda.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * v0.3.7 updated these hex values to match the operator-approved Figma Make
 * visual reference ("Design According to MD Document") — see DESIGN.md
 * ADR-105. This is a deliberate, scoped exception to ADR-094/095's "verbatim
 * mirror of app/globals.css" rule: the operator approved adopting Figma's
 * exact palette for Android only, without also touching the web app's
 * globals.css this round, so the two platforms now carry intentionally
 * different (though visually similar — same warm-paper/cinnabar-red/gold
 * family) hex values until a future round reconciles them if asked.
 *
 * Material 3 Dynamic Color remains intentionally unused for the reason
 * recorded in the original v0.3.1 note: dynamic color derives the palette
 * from the user's wallpaper and would silently override this brand palette
 * on Android 12+.
 */
val Paper = Color(0xFFFAF8F4)
val PaperSoft = Color(0xFFFFFFFF)
val PaperWarm = Color(0xFFFFF8E6)
val Ink = Color(0xFF1C1410)
val InkMuted = Color(0xFF3A3028)
val InkSoft = Color(0xFF7A6558)
val Cinnabar = Color(0xFFC1292E)
val CinnabarDeep = Color(0xFFA02226)
val Gold = Color(0xFFC9A84C)
val Sage = Color(0xFF667B5C)

/**
 * v0.3.8 addition: Material 3's `ColorScheme` has ~30 roles, and the
 * original set above only ever specified ~16 of them. Every role left
 * unspecified in `lightColorScheme(...)` silently falls back to Material's
 * baseline purple palette, not anything derived from our brand colors —
 * and `Card`'s default tonal-elevation surface blend uses exactly those
 * unspecified roles (`surfaceTint`, `surfaceContainer*`), which can resolve
 * to a color that doesn't byte-match any named role, making
 * `contentColorFor()` return an unexpected value instead of a neutral
 * ink tone. This surfaced as real, screenshot-confirmed bug: `Card`-embedded
 * `Text` with no explicit color rendered in a reddish tone (matching
 * `error`/`CinnabarDeep`) instead of `onSurface`/`Ink` — see DESIGN.md
 * ADR-112. These fill in the remaining roles so every part of the scheme is
 * deliberately chosen, not left to an off-brand baseline default.
 */
val InversePrimaryTint = Color(0xFFFFDAD4)
val SecondaryContainerTint = Color(0xFFFFF3D6)
val TertiaryContainerTint = Color(0xFFDCE8D1)
val OnTertiaryContainerDark = Color(0xFF1F2E17)
val ErrorContainerTint = Color(0xFFFFDAD4)
val OnErrorContainerDark = Color(0xFF410E0B)
val OutlineVariantLight = Color(0xFFD8CCC0)
val SurfaceBright = Color(0xFFFFFBF7)
val SurfaceDim = Color(0xFFE8E1D8)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFFAF3EA)
val SurfaceContainer = Color(0xFFF5EFE1)
val SurfaceContainerHigh = Color(0xFFEFE8DB)
val SurfaceContainerHighest = Color(0xFFE9E1D3)

/**
 * v0.3.13: per-category accent colors for the 6 Tools categories, matching
 * the web app's `TOOL_META` accent hex values exactly (`components/tools/
 * ToolsBoard.tsx`) — see DESIGN.md ADR-117. `ToolsAccentCurrency` reuses the
 * existing `Sage` constant since the web's currency accent (`#667b5c`) is
 * the same color.
 */
val ToolsAccentVisa = Color(0xFFA33A2D)
val ToolsAccentPayment = Color(0xFFB68634)
val ToolsAccentCurrency = Sage
val ToolsAccentMetro = Color(0xFF4A6080)
val ToolsAccentEsim = Color(0xFF7A5C8A)
val ToolsAccentEmergency = Color(0xFF9D2F24)
