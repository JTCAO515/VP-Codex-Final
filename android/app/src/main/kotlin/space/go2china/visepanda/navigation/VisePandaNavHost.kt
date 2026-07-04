package space.go2china.visepanda.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import space.go2china.visepanda.ui.butler.ButlerScreen
import space.go2china.visepanda.ui.explore.ExploreScreen
import space.go2china.visepanda.ui.me.MeScreen
import space.go2china.visepanda.ui.plan.DayDetailScreen
import space.go2china.visepanda.ui.tools.ToolsScreen
import space.go2china.visepanda.ui.trips.TripsScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import space.go2china.visepanda.ui.theme.Dimens
import space.go2china.visepanda.ui.theme.Ink
import space.go2china.visepanda.ui.theme.Paper
import space.go2china.visepanda.ui.translate.TranslateScreen

/**
 * v0.3.10: the bottom nav floats as a `Box` overlay on top of full-screen
 * content (operator directive — the nav bar should float over page content,
 * not push it up/reserve its own layout space). Previously this used
 * `Scaffold(bottomBar = ...)`, which pads `NavHost` away from the bar's
 * height; screens now render edge-to-edge and add
 * `Dimens.BottomNavContentClearance` bottom padding themselves where content
 * needs to clear the bar. See DESIGN.md ADR-114.
 */
@Composable
fun VisePandaApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val isTopLevelDestination = TopLevelDestination.all.any { it.route == backStackEntry?.destination?.route }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Butler.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(TopLevelDestination.Trips.route) {
                TripsScreen(
                    onAskButler = { navController.navigate(TopLevelDestination.Butler.route) },
                    onOpenDay = { dayNumber -> navController.navigate(DetailDestinations.dayDetailRoute(dayNumber)) },
                )
            }
            composable(TopLevelDestination.Butler.route) {
                ButlerScreen()
            }
            composable(TopLevelDestination.Explore.route) {
                ExploreScreen()
            }
            composable(TopLevelDestination.Tools.route) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ToolsScreen()
                    Button(
                        onClick = { navController.navigate(DetailDestinations.TRANSLATE_ROUTE) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Dimens.SpaceLG),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Ink,
                            contentColor = Paper
                        )
                    ) {
                        Text("👉 Enter Translator (v0.3.15 Test)")
                    }
                }
            }
            composable(DetailDestinations.TRANSLATE_ROUTE) {
                TranslateScreen(onBack = { navController.popBackStack() })
            }
            composable(TopLevelDestination.Me.route) {
                MeScreen()
            }
            composable(
                route = DetailDestinations.DAY_DETAIL_ROUTE,
                arguments = listOf(navArgument(DetailDestinations.DAY_NUMBER_ARG) { type = NavType.StringType }),
            ) {
                DayDetailScreen(onBack = { navController.popBackStack() })
            }
        }

        if (isTopLevelDestination) {
            VisePandaBottomBar(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
