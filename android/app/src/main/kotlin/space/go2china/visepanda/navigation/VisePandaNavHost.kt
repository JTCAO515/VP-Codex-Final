package space.go2china.visepanda.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import space.go2china.visepanda.ui.butler.ButlerScreen
import space.go2china.visepanda.ui.explore.ExploreScreen
import space.go2china.visepanda.ui.plan.DayDetailScreen
import space.go2china.visepanda.ui.plan.PlanScreen
import space.go2china.visepanda.ui.today.TodayScreen
import space.go2china.visepanda.ui.tools.ToolsScreen

@Composable
fun VisePandaApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val isTopLevelDestination = TopLevelDestination.all.any { it.route == backStackEntry?.destination?.route }

    Scaffold(
        bottomBar = { if (isTopLevelDestination) VisePandaBottomBar(navController) },
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Today.route,
            modifier = Modifier.padding(scaffoldPadding),
        ) {
            composable(TopLevelDestination.Today.route) {
                TodayScreen(onAskButler = {
                    navController.navigate(TopLevelDestination.Butler.route)
                })
            }
            composable(TopLevelDestination.Butler.route) {
                ButlerScreen()
            }
            composable(TopLevelDestination.Plan.route) {
                PlanScreen(onOpenDay = { dayNumber ->
                    navController.navigate(DetailDestinations.dayDetailRoute(dayNumber))
                })
            }
            composable(TopLevelDestination.Explore.route) {
                ExploreScreen()
            }
            composable(TopLevelDestination.Tools.route) {
                ToolsScreen()
            }
            composable(
                route = DetailDestinations.DAY_DETAIL_ROUTE,
                arguments = listOf(navArgument(DetailDestinations.DAY_NUMBER_ARG) { type = NavType.StringType }),
            ) {
                DayDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
