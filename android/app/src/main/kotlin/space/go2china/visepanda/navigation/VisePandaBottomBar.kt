package space.go2china.visepanda.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import space.go2china.visepanda.ui.theme.Ink
import space.go2china.visepanda.ui.theme.Paper

/**
 * v0.3.9: floating, fully-rounded pill container inset from all three
 * screen edges (operator-provided design reference — see DESIGN.md
 * ADR-113), replacing v0.3.8's full-width bar. Icon-only, no visible
 * labels (matches the reference; `contentDescription` still carries the
 * accessible name for screen readers). Dark (`Ink`) fill reads as a
 * deliberate contrast surface floating over the page background, not a
 * dark-theme switch — the rest of the app stays on the warm light palette.
 *
 * v0.3.10: `VisePandaNavHost.kt` renders this as a `Box` overlay on top of
 * full-screen content, not a Scaffold `bottomBar` slot that reserves layout
 * space — screen content now actually scrolls/renders behind this bar
 * instead of stopping short of it (DESIGN.md ADR-114). Screens add
 * `Dimens.BottomNavContentClearance` bottom padding so their content can
 * still clear the bar's footprint when scrolled/settled.
 */
@Composable
fun VisePandaBottomBar(navController: NavHostController, modifier: Modifier = Modifier) {
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.BottomNavHorizontalInset,
                vertical = Dimens.BottomNavBottomInset,
            ),
    ) {
        Surface(
            shape = RoundedCornerShape(Dimens.RadiusPill),
            color = Ink,
            shadowElevation = Dimens.SpaceSM,
            modifier = Modifier.fillMaxWidth().height(Dimens.BottomNavFloatingHeight),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpaceLG),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TopLevelDestination.leftOfCenter.forEach { destination ->
                    SideNavItem(destination, isSelected(destination)) { navigate(destination) }
                }
                // Reserves horizontal space so the side items don't crowd
                // under the floating Chat button.
                Box(modifier = Modifier.size(width = 52.dp, height = 1.dp))
                TopLevelDestination.rightOfCenter.forEach { destination ->
                    SideNavItem(destination, isSelected(destination)) { navigate(destination) }
                }
            }
        }

        CenterChatButton(
            onClick = { navigate(TopLevelDestination.Butler) },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SideNavItem(destination: TopLevelDestination, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else Paper.copy(alpha = 0.55f)
    Box(
        modifier = Modifier
            .size(Dimens.TouchTargetMin)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(destination.icon, contentDescription = stringResource(destination.labelRes), tint = tint)
    }
}

@Composable
private fun CenterChatButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val destination = TopLevelDestination.Butler
    Box(
        modifier = modifier
            .offset(y = (-10).dp)
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
}
