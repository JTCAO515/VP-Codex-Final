package space.go2china.visepanda.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import space.go2china.visepanda.ui.theme.Dimens

/**
 * Two side pairs (Trips/Explore, Tools/Me) flanking a raised, filled-circle
 * Chat button — the v0.3.8 nav restructure the operator asked for so Chat
 * reads as visually dominant, matching the Figma reference's bottom nav
 * shape. Deliberately not a plain Material3 `NavigationBar` (that's what
 * v0.3.3-v0.3.7 used) — see TopLevelDestination's doc comment and
 * DESIGN.md ADR-110.
 */
@Composable
fun VisePandaBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    fun isSelected(destination: TopLevelDestination) =
        currentDestination?.hierarchy?.any { it.route == destination.route } == true

    fun navigate(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Dimens.SpaceXS,
            modifier = Modifier.fillMaxWidth().height(Dimens.BottomNavHeight),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TopLevelDestination.leftOfCenter.forEach { destination ->
                    SideNavItem(destination, isSelected(destination)) { navigate(destination) }
                }
                // Reserves horizontal space so the side items don't crowd
                // under the floating Chat button.
                Box(modifier = Modifier.size(width = 58.dp, height = 1.dp))
                TopLevelDestination.rightOfCenter.forEach { destination ->
                    SideNavItem(destination, isSelected(destination)) { navigate(destination) }
                }
            }
        }

        CenterChatButton(
            selected = isSelected(TopLevelDestination.Butler),
            onClick = { navigate(TopLevelDestination.Butler) },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SideNavItem(destination: TopLevelDestination, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(destination.icon, contentDescription = stringResource(destination.labelRes), tint = tint)
        Text(
            text = stringResource(destination.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
        )
    }
}

@Composable
private fun CenterChatButton(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val destination = TopLevelDestination.Butler
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier.offset(y = (-14).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = stringResource(destination.labelRes),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Text(
            text = stringResource(destination.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
        )
    }
}
