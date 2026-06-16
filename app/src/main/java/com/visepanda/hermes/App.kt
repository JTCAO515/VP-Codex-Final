package com.visepanda.hermes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.components.BottomNavTab
import com.visepanda.designsystem.components.VpBottomNav
import com.visepanda.hermes.ui.home.HomeScreen
import com.visepanda.hermes.ui.explore.ExploreScreen
import com.visepanda.hermes.ui.chat.ChatScreen
import com.visepanda.hermes.ui.trips.TripsScreen

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
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (selectedTab) {
            BottomNavTab.HOME -> HomeScreen(modifier = contentModifier)
            BottomNavTab.EXPLORE -> ExploreScreen(modifier = contentModifier)
            BottomNavTab.CHAT -> ChatScreen(modifier = contentModifier)
            BottomNavTab.TRIPS -> TripsScreen(modifier = contentModifier)
        }
    }
}
