package space.go2china.visepanda.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import space.go2china.visepanda.R

/**
 * v0.3.8 bottom-nav restructure (operator directive, following the v0.3.7
 * Figma visual pass): Trips / Explore / Chat / Tools / Me, rendered as two
 * side pairs flanking a raised center Chat button — not a plain five-item
 * horizontal `NavigationBar` like v0.3.3-v0.3.7 used. `Trips` merges the
 * former `Today` (Now/Next/Later) and `Plan` (day-by-day) surfaces into one
 * destination; `Me` is a new profile/settings surface. See DESIGN.md
 * ADR-110/ADR-111 for the rationale and what `Me` honestly does and
 * doesn't show yet.
 */
sealed class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    data object Trips : TopLevelDestination("trips", R.string.nav_trips, Icons.Filled.CardTravel)
    data object Explore : TopLevelDestination("explore", R.string.nav_explore, Icons.Filled.Explore)
    data object Butler : TopLevelDestination("butler", R.string.nav_butler, Icons.Filled.Chat)
    data object Tools : TopLevelDestination("tools", R.string.nav_tools, Icons.Filled.Handyman)
    data object Me : TopLevelDestination("me", R.string.nav_me, Icons.Filled.Person)

    companion object {
        val all = listOf(Trips, Explore, Butler, Tools, Me)

        /** Rendered left-to-right on the left side of the floating Chat button. */
        val leftOfCenter = listOf(Trips, Explore)

        /** Rendered left-to-right on the right side of the floating Chat button. */
        val rightOfCenter = listOf(Tools, Me)
    }
}

/** Non-bottom-nav destinations, reached by navigating from a top-level surface. */
object DetailDestinations {
    const val DAY_DETAIL_ROUTE = "day_detail/{dayNumber}"
    const val DAY_NUMBER_ARG = "dayNumber"
    const val TRANSLATE_ROUTE = "translate"

    fun dayDetailRoute(dayNumber: Int): String = "day_detail/$dayNumber"
}
