package space.jtcao.visepanda.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom navigation items matching the web version's 5 tabs.
 * Home · Chat · Map · Trips · Tools (Cities accessed from Home and nested nav)
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(Routes.HOME, "Home", Icons.Default.Home),
    CHAT(Routes.CHAT, "Chat", Icons.Default.Chat),
    MAP(Routes.MAP, "Map", Icons.Default.Map),
    TRIPS(Routes.TRIPS, "Trips", Icons.Default.ListAlt),
    TOOLS(Routes.TOOLS, "Tools", Icons.Default.Build);

    companion object {
        val bottomBarRoutes = entries.map { it.route }.toSet()
    }
}
