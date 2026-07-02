package space.go2china.visepanda.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Ported verbatim from app/globals.css :root (Warm New Chinese palette) and
 * kept in sync by hand with res/values/colors.xml — see DESIGN.md v0.3.1
 * note on why Material 3 Dynamic Color is intentionally not used here:
 * dynamic color derives the palette from the user's wallpaper and would
 * silently override this brand palette on Android 12+, which is the
 * opposite of "keep the same paper/cinnabar/gold/sage identity across web
 * and native."
 */
val Paper = Color(0xFFF7EAD5)
val PaperSoft = Color(0xFFFBF4E8)
val PaperWarm = Color(0xFFF0D9BB)
val Ink = Color(0xFF2E251D)
val InkMuted = Color(0xFF6F5B49)
val InkSoft = Color(0xFF947F69)
val Cinnabar = Color(0xFFA33A2D)
val CinnabarDeep = Color(0xFF7D2A21)
val Gold = Color(0xFFB68634)
val Sage = Color(0xFF667B5C)
