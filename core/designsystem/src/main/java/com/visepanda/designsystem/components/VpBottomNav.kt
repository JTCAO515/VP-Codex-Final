package com.visepanda.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.SurfaceDefault
import com.visepanda.designsystem.TextPrimary

enum class BottomNavTab(val label: String) {
    HOME("Home"),
    EXPLORE("Explore"),
    CHAT("Chat"),
    TRIPS("Trips")
}

data class BottomNavItem(
    val tab: BottomNavTab,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

private val navItems = listOf(
    BottomNavItem(BottomNavTab.HOME, Icons.Outlined.Home, Icons.Filled.Home),
    BottomNavItem(BottomNavTab.EXPLORE, Icons.Outlined.Explore, Icons.Filled.Explore),
    BottomNavItem(BottomNavTab.CHAT, Icons.Outlined.Chat, Icons.Filled.Chat),
    BottomNavItem(BottomNavTab.TRIPS, Icons.Outlined.Work, Icons.Filled.Work)
)

@Composable
fun VpBottomNav(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDefault,
        tonalElevation = 1.dp
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = item.tab == selectedTab,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (item.tab == selectedTab) item.selectedIcon else item.icon,
                        contentDescription = item.tab.label
                    )
                },
                label = {
                    Text(
                        text = item.tab.label,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    unselectedIconColor = JadeGrey,
                    unselectedTextColor = JadeGrey,
                    indicatorColor = Gold.copy(alpha = 0.08f)
                )
            )
        }
    }
}
