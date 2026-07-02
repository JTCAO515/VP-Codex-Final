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
