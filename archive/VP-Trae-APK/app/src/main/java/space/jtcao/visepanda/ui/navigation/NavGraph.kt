package space.jtcao.visepanda.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import space.jtcao.visepanda.ui.home.HomeScreen
import space.jtcao.visepanda.ui.chat.ChatScreen
import space.jtcao.visepanda.ui.map.MapScreen
import space.jtcao.visepanda.ui.trips.TripsScreen
import space.jtcao.visepanda.ui.cities.CityDetailScreen
import space.jtcao.visepanda.ui.cities.CityListScreen
import space.jtcao.visepanda.ui.tools.ToolsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        // ── Home ──
        composable(Routes.HOME) {
            HomeScreen(
                onCityClick = { cityName ->
                    navController.navigate(Routes.cityDetail(cityName))
                },
                onViewAllCities = {
                    navController.navigate(Routes.CITIES)
                },
                onStartChat = {
                    navController.navigate(Routes.CHAT)
                }
            )
        }

        // ── Chat ──
        composable(Routes.CHAT) {
            ChatScreen(city = null)
        }
        composable(
            route = Routes.CHAT_CITY,
            arguments = listOf(navArgument("city") { type = NavType.StringType })
        ) { backStackEntry ->
            ChatScreen(city = backStackEntry.arguments?.getString("city"))
        }

        // ── Map ──
        composable(Routes.MAP) {
            MapScreen(
                onCityClick = { cityName ->
                    navController.navigate(Routes.cityDetail(cityName))
                }
            )
        }

        // ── Trips ──
        composable(Routes.TRIPS) {
            TripsScreen(
                onStartPlanning = {
                    navController.navigate(Routes.CHAT)
                }
            )
        }

        // ── Cities ──
        composable(Routes.CITIES) {
            CityListScreen(
                onCityClick = { cityName ->
                    navController.navigate(Routes.cityDetail(cityName))
                }
            )
        }
        composable(
            route = Routes.CITY_DETAIL,
            arguments = listOf(navArgument("cityName") { type = NavType.StringType })
        ) { backStackEntry ->
            CityDetailScreen(
                cityName = backStackEntry.arguments?.getString("cityName") ?: "",
                onBack = { navController.popBackStack() },
                onPlanTrip = { cityName ->
                    navController.navigate(Routes.chatCity(cityName))
                }
            )
        }

        // ── Tools ──
        composable(Routes.TOOLS) {
            ToolsScreen()
        }
    }
}
