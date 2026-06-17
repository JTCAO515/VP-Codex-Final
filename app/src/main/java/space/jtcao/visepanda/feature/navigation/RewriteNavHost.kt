package space.jtcao.visepanda.feature.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import space.jtcao.visepanda.core.designsystem.components.VpScaffold
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader
import space.jtcao.visepanda.data.auth.SupabaseRecoveryCoordinator
import space.jtcao.visepanda.feature.auth.AccountScreen
import space.jtcao.visepanda.feature.auth.ForgotPasswordScreen
import space.jtcao.visepanda.feature.auth.LoginScreen
import space.jtcao.visepanda.feature.auth.RegisterScreen
import space.jtcao.visepanda.feature.auth.ResetPasswordScreen
import space.jtcao.visepanda.feature.chat.ChatScreen
import space.jtcao.visepanda.feature.destination.CityDetailScreen
import space.jtcao.visepanda.feature.explore.ExploreScreen
import space.jtcao.visepanda.feature.home.HomeScreen
import space.jtcao.visepanda.feature.tools.ToolsScreen
import space.jtcao.visepanda.feature.trips.TripsScreen

@Composable
fun RewriteNavHost() {
    val navController = rememberNavController()
    val tabs = listOf("Home", "Explore", "Chat", "Trips")
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route ?: RewriteRoutes.HOME
    val pendingRecovery by SupabaseRecoveryCoordinator.pendingRecovery.collectAsState()

    LaunchedEffect(pendingRecovery) {
        if (pendingRecovery) {
            navController.navigate(RewriteRoutes.RESET_PASSWORD) {
                launchSingleTop = true
            }
            SupabaseRecoveryCoordinator.consumeRecovery()
        }
    }

    VpScaffold(
        tabs = tabs,
        selectedTab = currentTabLabel(currentRoute),
        onTabSelected = { tab ->
            val route = when (tab) {
                "Explore" -> RewriteRoutes.EXPLORE
                "Chat" -> RewriteRoutes.CHAT
                "Trips" -> RewriteRoutes.TRIPS
                else -> RewriteRoutes.HOME
            }
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = RewriteRoutes.HOME
        ) {
            composable(RewriteRoutes.HOME) {
                HomeScreen(
                    onOpenExplore = { navController.navigate(RewriteRoutes.EXPLORE) },
                    onOpenDestination = { cityId ->
                        navController.navigate(featuredDestinationRoute(cityId))
                    },
                    onOpenChat = { navController.navigate(RewriteRoutes.CHAT) },
                    onOpenTools = { navController.navigate(RewriteRoutes.TOOLS) },
                    onOpenAccount = { navController.navigate(RewriteRoutes.ACCOUNT) }
                )
            }
            composable(RewriteRoutes.ACCOUNT) {
                AccountScreen(
                    onOpenLogin = { navController.navigate(RewriteRoutes.LOGIN) },
                    onOpenRegister = { navController.navigate(RewriteRoutes.REGISTER) },
                    onBackToHome = {
                        navController.navigate(RewriteRoutes.HOME) {
                            popUpTo(RewriteRoutes.HOME)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(RewriteRoutes.LOGIN) {
                LoginScreen(
                    onOpenRegister = { navController.navigate(RewriteRoutes.REGISTER) },
                    onOpenForgotPassword = { navController.navigate(RewriteRoutes.FORGOT_PASSWORD) },
                    onAuthSuccess = {
                        navController.navigate(RewriteRoutes.ACCOUNT) {
                            popUpTo(RewriteRoutes.LOGIN) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onBackToHome = {
                        navController.navigate(RewriteRoutes.HOME) {
                            popUpTo(RewriteRoutes.HOME)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(RewriteRoutes.REGISTER) {
                RegisterScreen(
                    onOpenLogin = { navController.navigate(RewriteRoutes.LOGIN) },
                    onRegistered = {
                        navController.navigate(RewriteRoutes.ACCOUNT) {
                            popUpTo(RewriteRoutes.REGISTER) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onBackToHome = {
                        navController.navigate(RewriteRoutes.HOME) {
                            popUpTo(RewriteRoutes.HOME)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(RewriteRoutes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    onOpenLogin = { navController.navigate(RewriteRoutes.LOGIN) },
                    onBackToHome = {
                        navController.navigate(RewriteRoutes.HOME) {
                            popUpTo(RewriteRoutes.HOME)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(RewriteRoutes.RESET_PASSWORD) {
                ResetPasswordScreen(
                    onOpenLogin = {
                        navController.navigate(RewriteRoutes.LOGIN) {
                            popUpTo(RewriteRoutes.RESET_PASSWORD) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onBackToHome = {
                        navController.navigate(RewriteRoutes.HOME) {
                            popUpTo(RewriteRoutes.HOME)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(RewriteRoutes.EXPLORE) {
                ExploreScreen(
                    onOpenDestination = { cityId ->
                        navController.navigate(RewriteRoutes.destination(cityId))
                    }
                )
            }
            composable(RewriteRoutes.CHAT) {
                ChatScreen()
            }
            composable(
                route = RewriteRoutes.CHAT_WITH_CITY,
                arguments = listOf(navArgument("cityId") { type = NavType.StringType })
            ) { backStack ->
                ChatScreen(
                    cityId = backStack.arguments?.getString("cityId")
                )
            }
            composable(RewriteRoutes.TRIPS) {
                TripsScreen(
                    onStartPlanning = {
                        navController.navigate(RewriteRoutes.CHAT) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(RewriteRoutes.TOOLS) {
                ToolsScreen()
            }
            composable(
                route = RewriteRoutes.DESTINATION,
                arguments = listOf(navArgument("cityId") { type = NavType.StringType })
            ) { backStack ->
                CityDetailScreen(
                    cityId = backStack.arguments?.getString("cityId").orEmpty(),
                    onPlanTrip = { cityId ->
                        navController.navigate(RewriteRoutes.chat(cityId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

internal fun featuredDestinationRoute(cityId: String): String = RewriteRoutes.destination(cityId)

private fun currentTabLabel(route: String): String = when {
    route == RewriteRoutes.EXPLORE -> "Explore"
    route == RewriteRoutes.DESTINATION || route.startsWith("destination/") -> "Explore"
    route == RewriteRoutes.CHAT || route.startsWith("chat/") -> "Chat"
    route == RewriteRoutes.TRIPS -> "Trips"
    route == RewriteRoutes.ACCOUNT -> "Home"
    route == RewriteRoutes.RESET_PASSWORD -> "Home"
    route == RewriteRoutes.TOOLS -> "Home"
    else -> "Home"
}

@Composable
private fun RewritePlaceholderScreen(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VpSectionHeader(
            title = title,
            subtitle = subtitle
        )
        Text(
            text = "当前页面已通过 Navigation Compose 挂到 RewriteNavHost 下。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
