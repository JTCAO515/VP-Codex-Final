package space.go2china.visepanda.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.graphics.vector.ImageVector
import space.go2china.visepanda.R

/**
 * The five product surfaces from the v0.3.2 synthesis, refined for the
 * mobile-first v0.3.6 shell as Today / Plan / Chat / Explore / Tools so
 * Chat sits in the center and opens first. This replaces the earlier v0.3.1 draft's more
 * implementation-flavored Canvas/Chat/Explore/Tools four-tab model — see
 * docs/planning/v0.3.2-android-planning-synthesis.md "Four Tabs Are Too
 * Technical".
 */
sealed class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    data object Today : TopLevelDestination("today", R.string.nav_today, Icons.Filled.Today)
    data object Plan : TopLevelDestination("plan", R.string.nav_plan, Icons.Filled.Map)
    data object Butler : TopLevelDestination("butler", R.string.nav_butler, Icons.Filled.Chat)
    data object Explore : TopLevelDestination("explore", R.string.nav_explore, Icons.Filled.Explore)
    data object Tools : TopLevelDestination("tools", R.string.nav_tools, Icons.Filled.Handyman)

    companion object {
        val all = listOf(Today, Butler, Plan, Explore, Tools)
    }
}

/** Non-bottom-nav destinations, reached by navigating from a top-level surface. */
object DetailDestinations {
    const val DAY_DETAIL_ROUTE = "day_detail/{dayNumber}"
    const val DAY_NUMBER_ARG = "dayNumber"

    fun dayDetailRoute(dayNumber: Int): String = "day_detail/$dayNumber"
}
