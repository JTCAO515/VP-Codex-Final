package com.visepanda.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.*
import com.visepanda.core.designsystem.component.*
import com.visepanda.core.designsystem.spacing.VpSpacing
import com.visepanda.core.designsystem.theme.VisePandaTheme

@Composable
fun HomeScreen(
    onCityClick: (String) -> Unit = {},
    onChatClick: () -> Unit = {},
    onToolsClick: () -> Unit = {},
) {
    VisePandaTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = VpSpacing.lg),
        ) {
            // Hero Section
            Spacer(modifier = Modifier.height(VpSpacing.xxl))

            Text(
                text = "VisePanda",
                style = MaterialTheme.typography.displayLarge,
                color = GoldPrimary,
            )
            Text(
                text = "Your AI China Travel Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(VpSpacing.xl))

            // Main CTA
            VpPrimaryButton(text = "Plan Your Trip", onClick = onChatClick)

            Spacer(modifier = Modifier.height(VpSpacing.xxl))

            // Featured Cities
            VpSectionHeader(title = "Featured Cities")

            Spacer(modifier = Modifier.height(VpSpacing.sm))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(5) { index ->
                    Surface(
                        modifier = Modifier.width(200.dp).height(140.dp),
                        shape = com.visepanda.core.designsystem.shape.VpShape.md,
                        color = SurfaceElevated,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "City ${index + 1}",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(VpSpacing.xxl))

            // Chat Entry
            VpCard(title = "Chat with Panda", description = "Plan your perfect China itinerary") {
                Spacer(modifier = Modifier.height(VpSpacing.sm))
                VpPrimaryButton(text = "Start Chat", onClick = onChatClick)
            }

            Spacer(modifier = Modifier.height(VpSpacing.xxl))

            // Tools Entry
            VpCard(title = "Travel Tools", description = "Visa, payment, SIM, and more") {
                Spacer(modifier = Modifier.height(VpSpacing.sm))
                VpSecondaryButton(text = "Explore Tools", onClick = onToolsClick)
            }
        }
    }
}
