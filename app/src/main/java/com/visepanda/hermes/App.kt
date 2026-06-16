package com.visepanda.hermes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.VisePandaElevation
import com.visepanda.designsystem.components.BottomNavTab
import com.visepanda.designsystem.components.VpBottomNav
import com.visepanda.hermes.ui.home.HomeScreen
import com.visepanda.hermes.ui.explore.ExploreScreen
import com.visepanda.hermes.ui.chat.ChatScreen
import com.visepanda.hermes.ui.trips.TripsScreen
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

// Tab indices for animated transitions
private val tabOrder = listOf(BottomNavTab.HOME, BottomNavTab.EXPLORE, BottomNavTab.CHAT, BottomNavTab.TRIPS)

@Composable
fun App(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }

    Scaffold(
        modifier = modifier,
        containerColor = Background,
        bottomBar = {
            VpBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
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
                        .clickable { selectedTab = BottomNavTab.CHAT },
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
