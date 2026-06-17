package com.visepanda.feature.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.*
import com.visepanda.core.designsystem.component.VpSectionHeader
import com.visepanda.core.designsystem.spacing.VpSpacing

data class ToolItem(val name: String, val icon: String, val description: String)

val tools = listOf(
    ToolItem("Payment", "💳", "Mobile payments & cards"),
    ToolItem("Visa", "🛂", "Entry requirements"),
    ToolItem("SIM & Internet", "📱", "Stay connected"),
    ToolItem("Emergency", "🚨", "Important contacts"),
    ToolItem("Etiquette", "🎌", "Cultural tips"),
    ToolItem("Phrases", "🗣️", "Useful Chinese"),
    ToolItem("Packing", "🧳", "What to bring"),
    ToolItem("Museums", "🏛️", "Top museums"),
)

@Composable
fun ToolsScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(VpSpacing.lg)) {
        VpSectionHeader(title = "Travel Tools")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tools) { tool ->
                Surface(
                    modifier = Modifier.height(120.dp).fillMaxWidth(),
                    shape = com.visepanda.core.designsystem.shape.VpShape.md,
                    color = SurfaceElevated,
                ) {
                    Column(
                        modifier = Modifier.padding(VpSpacing.md).fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = tool.icon, style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = tool.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        }
    }
}
