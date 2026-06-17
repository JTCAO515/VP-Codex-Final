package com.visepanda.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.visepanda.app.navigation.Routes
import com.visepanda.core.designsystem.component.BottomNavTab
import com.visepanda.core.designsystem.component.VpBottomNav
import com.visepanda.core.designsystem.color.SurfaceDefault
import com.visepanda.core.designsystem.theme.VisePandaTheme
import com.visepanda.feature.home.HomeScreen
import com.visepanda.feature.explore.ExploreScreen
import com.visepanda.feature.chat.ChatScreen
import com.visepanda.feature.trips.TripsScreen
import com.visepanda.feature.city.CityDetailScreen
import com.visepanda.feature.tools.ToolsScreen
import com.visepanda.feature.auth.AuthScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VisePandaTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(BottomNavTab.Home) }

    val tabsWithRoutes = mapOf(
        BottomNavTab.Home to Routes.HOME,
        BottomNavTab.Explore to Routes.EXPLORE,
        BottomNavTab.Chat to Routes.CHAT,
        BottomNavTab.Trips to Routes.TRIPS,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceDefault,
        bottomBar = {
            VpBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    navController.navigate(tabsWithRoutes[tab]!!) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
            ) {
                composable(Routes.HOME) {
                    selectedTab = BottomNavTab.Home
                    HomeScreen(
                        onCityClick = { cityId -> navController.navigate("city/$cityId") },
                        onChatClick = { navController.navigate(Routes.CHAT) },
                        onToolsClick = { navController.navigate(Routes.TOOLS) },
                    )
                }
                composable(Routes.EXPLORE) {
                    selectedTab = BottomNavTab.Explore
                    ExploreScreen(
                        onCityClick = { cityId -> navController.navigate("city/$cityId") },
                    )
                }
                composable(Routes.CHAT) {
                    selectedTab = BottomNavTab.Chat
                    ChatScreen()
                }
                composable(Routes.TRIPS) {
                    selectedTab = BottomNavTab.Trips
                    TripsScreen(
                        onStartPlanning = { navController.navigate(Routes.CHAT) },
                    )
                }
                composable("city/{cityId}") { backStackEntry ->
                    val cityId = backStackEntry.arguments?.getString("cityId") ?: ""
                    CityDetailScreen(
                        cityId = cityId,
                        onPlanClick = { navController.navigate(Routes.CHAT) },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.TOOLS) {
                    ToolsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.AUTH) {
                    AuthScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
