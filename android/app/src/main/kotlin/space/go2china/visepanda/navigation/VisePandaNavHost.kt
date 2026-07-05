package space.go2china.visepanda.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import space.go2china.visepanda.ui.AppLanguageViewModel
import space.go2china.visepanda.ui.LocalizedApp
import space.go2china.visepanda.ui.butler.ButlerScreen
import space.go2china.visepanda.ui.explore.ExploreScreen
import space.go2china.visepanda.ui.me.MeScreen
import space.go2china.visepanda.ui.plan.DayDetailScreen
import space.go2china.visepanda.ui.tools.ToolCategoryDetailScreen
import space.go2china.visepanda.ui.tools.ToolsScreen
import space.go2china.visepanda.ui.trips.TripsScreen
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
    val currentRoute = backStackEntry?.destination?.route?.split("?")?.firstOrNull()
    val isTopLevelDestination = TopLevelDestination.all.any { it.route == currentRoute }

    // v0.3.14: scoped to the Activity (this composable, not any single nav
    // route) so Me's language toggle affects every screen — see DESIGN.md
    // ADR-118.
    val languageViewModel: AppLanguageViewModel = hiltViewModel()
    val languageCode by languageViewModel.languageCode.collectAsState()

    LocalizedApp(languageCode = languageCode) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "${TopLevelDestination.Butler.route}?message={message}",
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(TopLevelDestination.Trips.route) {
                TripsScreen(
                    onAskButler = { navController.navigate(TopLevelDestination.Butler.route) },
                    onOpenDay = { dayNumber -> navController.navigate(DetailDestinations.dayDetailRoute(dayNumber)) },
                )
            }
            composable(
                route = "${TopLevelDestination.Butler.route}?message={message}",
                arguments = listOf(navArgument("message") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                ButlerScreen(
                    onOpenToolCategory = { categoryId ->
                        if (categoryId == "language" || categoryId == "translate") {
                            navController.navigate(DetailDestinations.TRANSLATE_ROUTE)
                        } else {
                            navController.navigate(DetailDestinations.toolCategoryRoute(categoryId))
                        }
                    },
                )
            }
            composable(TopLevelDestination.Explore.route) {
                ExploreScreen()
            }
            composable(TopLevelDestination.Tools.route) {
                ToolsScreen(
                    onOpenCategory = { categoryId ->
                        if (categoryId == "language" || categoryId == "translate") {
                            navController.navigate(DetailDestinations.TRANSLATE_ROUTE)
                        } else {
                            navController.navigate(DetailDestinations.toolCategoryRoute(categoryId))
                        }
                    },
                )
            }
            composable(TopLevelDestination.Me.route) {
                MeScreen(
                    languageCode = languageCode,
                    onSelectLanguage = languageViewModel::setLanguage,
                )
            }
            composable(
                route = DetailDestinations.DAY_DETAIL_ROUTE,
                arguments = listOf(navArgument(DetailDestinations.DAY_NUMBER_ARG) { type = NavType.StringType }),
            ) {
                DayDetailScreen(
                    onBack = { navController.popBackStack() },
                    onScheduleCandidate = { day, block ->
                        val message = "Schedule ${block.title} into Day ${day.day} in ${day.city}. Keep the route practical, choose the best time slot, and explain what changed."
                        navController.navigate("butler?message=${android.net.Uri.encode(message)}") {
                            popUpTo(TopLevelDestination.Butler.route) {
                                inclusive = false
                            }
                        }
                    }
                )
            }
            composable(
                route = DetailDestinations.TOOL_CATEGORY_ROUTE,
                arguments = listOf(navArgument(DetailDestinations.TOOL_CATEGORY_ARG) { type = NavType.StringType }),
            ) {
                ToolCategoryDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(DetailDestinations.TRANSLATE_ROUTE) {
                TranslateScreen(onBack = { navController.popBackStack() })
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
}
