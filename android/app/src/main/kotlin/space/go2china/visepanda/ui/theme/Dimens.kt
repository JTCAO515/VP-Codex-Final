package space.go2china.visepanda.ui.theme

import androidx.compose.ui.unit.dp

/**
 * dp token set mapped from the web app's spacing/radius scale — see
 * docs/planning/v0.3.1-android-native-spec.md section 4.2.1 and the earlier
 * v0.3.1 supplementary draft's token table. Composables must reference these
 * constants instead of hardcoded `.dp` literals (mirrors the same rule the
 * web app enforces with CSS custom properties in app/globals.css).
 */
object Dimens {
    val SpaceXS = 4.dp
    val SpaceSM = 8.dp
    val SpaceMD = 12.dp
    val SpaceLG = 16.dp
    val SpaceXL = 24.dp

    val RadiusSM = 6.dp
    val RadiusMD = 8.dp
    val RadiusLG = 16.dp
    val RadiusXL = 20.dp
    val RadiusPill = 999.dp

    val TouchTargetMin = 48.dp

    val BottomNavHeight = 80.dp

    /**
     * v0.3.9: the floating pill-shaped bottom nav (operator-provided design
     * reference — see DESIGN.md ADR-113) sits inset from all three screen
     * edges, not flush like the v0.3.8 full-width bar.
     */
    val BottomNavFloatingHeight = 64.dp
    val BottomNavHorizontalInset = 16.dp
    val BottomNavBottomInset = 16.dp
}
