package com.visepanda.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.Divider
import com.visepanda.core.designsystem.color.GoldPrimary
import com.visepanda.core.designsystem.color.JadeGrey
import com.visepanda.core.designsystem.color.SurfaceDefault
import com.visepanda.core.designsystem.color.TextPrimary
import com.visepanda.core.designsystem.spacing.VpSpacing

enum class BottomNavTab(val icon: ImageVector, val label: String) {
    Home(Icons.Filled.Home, "Home"),
    Explore(Icons.Filled.Explore, "Explore"),
    Chat(Icons.Filled.Chat, "Chat"),
    Trips(Icons.Filled.WorkspacePremium, "Trips"),
}

@Composable
fun VpBottomNav(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceDefault,
        shadowElevation = 8.dp,
    ) {
        Column {
            // Top divider
            Spacer(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Divider))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) GoldPrimary else JadeGrey,
                        animationSpec = spring(),
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { onTabSelected(tab) },
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = tab.label,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = iconColor,
                        )
                    }
                }
            }
        }
    }
}
