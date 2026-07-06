package com.visepanda.hermes

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.VisePandaElevation
import com.visepanda.designsystem.components.BottomNavTab
import com.visepanda.designsystem.components.VpBottomNav
import com.visepanda.hermes.data.AuthRepository
import com.visepanda.hermes.ui.auth.AuthScreen
import com.visepanda.hermes.ui.home.HomeScreen
import com.visepanda.hermes.ui.explore.ExploreScreen
import com.visepanda.hermes.ui.chat.ChatScreen
import com.visepanda.hermes.ui.trips.TripsScreen
import kotlinx.coroutines.delay

// ── App entry state ──

private enum class AppScreen { SPLASH, AUTH, MAIN }

// Tab indices for animated transitions
private val tabOrder = listOf(BottomNavTab.HOME, BottomNavTab.EXPLORE, BottomNavTab.CHAT, BottomNavTab.TRIPS)

@Composable
fun App(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { AuthRepository(context) }

    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }

    // Check auth state on launch
    LaunchedEffect(Unit) {
        delay(600) // splash delay

        if (repo.isLoggedIn()) {
            // Try to verify token
            val token = repo.getToken() ?: ""
            val result = repo.verifyToken(token)
            if (result.isSuccess) {
                currentScreen = AppScreen.MAIN
            } else {
                // Token expired — clear and show auth
                repo.logout()
                currentScreen = AppScreen.AUTH
            }
        } else {
            currentScreen = AppScreen.AUTH
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
        },
        label = "appScreen",
        modifier = modifier
    ) { screen ->
        when (screen) {
            AppScreen.SPLASH -> SplashScreen()
            AppScreen.AUTH -> AuthScreen(
                onAuthSuccess = { currentScreen = AppScreen.MAIN }
            )
            AppScreen.MAIN -> MainApp(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Gold),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "V",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MainApp(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    Scaffold(
        containerColor = Background,
        bottomBar = {
            VpBottomNav(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        },
        floatingActionButton = {
            if (selectedTab != BottomNavTab.CHAT) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(52.dp)
                        .shadow(
                            elevation = VisePandaElevation.fab,
                            shape = CircleShape,
                            ambientColor = Color(0x33C9A96E),
                            spotColor = Color(0x26C9A96E)
                        )
                        .clip(CircleShape)
                        .background(Gold)
                        .clickable { onTabSelected(BottomNavTab.CHAT) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83D\uDCAC", fontSize = 20.sp)
                }
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        // Animated page transitions
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val currentIdx = tabOrder.indexOf(initialState)
                val targetIdx = tabOrder.indexOf(targetState)
                val direction = if (targetIdx > currentIdx) 1 else -1

                (slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> direction * fullWidth / 4 }
                ) + fadeIn(animationSpec = tween(200)))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { fullWidth -> -direction * fullWidth / 4 }
                        ) + fadeOut(animationSpec = tween(200))
                    )
            },
            label = "pageTransition",
            modifier = contentModifier
        ) { tab ->
            when (tab) {
                BottomNavTab.HOME -> HomeScreen()
                BottomNavTab.EXPLORE -> ExploreScreen()
                BottomNavTab.CHAT -> ChatScreen()
                BottomNavTab.TRIPS -> TripsScreen()
            }
        }
    }
}
