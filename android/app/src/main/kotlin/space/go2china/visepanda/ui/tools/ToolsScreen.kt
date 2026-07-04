package space.go2china.visepanda.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.data.model.ToolCategory
import space.go2china.visepanda.ui.components.LoadingStateView
import space.go2china.visepanda.ui.theme.Dimens
import space.go2china.visepanda.ui.theme.ToolsAccentCurrency
import space.go2china.visepanda.ui.theme.ToolsAccentEmergency
import space.go2china.visepanda.ui.theme.ToolsAccentEsim
import space.go2china.visepanda.ui.theme.ToolsAccentMetro
import space.go2china.visepanda.ui.theme.ToolsAccentPayment
import space.go2china.visepanda.ui.theme.ToolsAccentVisa
import space.go2china.visepanda.ui.theme.ToolsAccentTranslate
import space.go2china.visepanda.ui.theme.ToolsAccentBooking

private data class ToolCategoryMeta(val icon: ImageVector, val accent: Color, val badge: String)

/**
 * v0.3.13: 1:1 content mirror of the web's `TOOL_META` map
 * (`components/tools/ToolsBoard.tsx`) — icon/accent/badge per category id.
 * See DESIGN.md ADR-117.
 */
private val toolCategoryMeta = mapOf(
    "visa-and-entry" to ToolCategoryMeta(Icons.Filled.FactCheck, ToolsAccentVisa, "Required"),
    "payment-setup" to ToolCategoryMeta(Icons.Filled.CreditCard, ToolsAccentPayment, "Pre-trip"),
    "currency" to ToolCategoryMeta(Icons.Filled.AttachMoney, ToolsAccentCurrency, "Live"),
    "metro" to ToolCategoryMeta(Icons.Filled.Train, ToolsAccentMetro, "Transit"),
    "esim-vpn" to ToolCategoryMeta(Icons.Filled.SimCard, ToolsAccentEsim, "Connectivity"),
    "emergency" to ToolCategoryMeta(Icons.Filled.Warning, ToolsAccentEmergency, "Emergency"),
    "translate" to ToolCategoryMeta(Icons.Filled.Translate, ToolsAccentTranslate, "Utility"),
    "booking" to ToolCategoryMeta(Icons.Filled.ConfirmationNumber, ToolsAccentBooking, "Info only"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onOpenCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ToolsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tools_title)) }) },
    ) { innerPadding ->
        when (val current = state) {
            is ToolsUiState.Loading -> LoadingStateView(modifier = Modifier.padding(innerPadding))
            is ToolsUiState.Content -> ToolsGrid(
                categories = current.categories,
                onOpenCategory = onOpenCategory,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun ToolsGrid(
    categories: List<ToolCategory>,
    onOpenCategory: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(contentPadding),
        contentPadding = PaddingValues(
            start = Dimens.SpaceLG,
            end = Dimens.SpaceLG,
            top = Dimens.SpaceLG,
            bottom = Dimens.BottomNavContentClearance,
        ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
    ) {
        items(categories, key = { it.id }) { category ->
            ToolCategoryCard(category = category, onClick = { onOpenCategory(category.id) })
        }
    }
}

@Composable
private fun ToolCategoryCard(category: ToolCategory, onClick: () -> Unit) {
    val meta = toolCategoryMeta[category.id]
    val accent = meta?.accent ?: MaterialTheme.colorScheme.primary
    Card(
        shape = RoundedCornerShape(Dimens.RadiusLG),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (meta != null) {
                    Icon(meta.icon, contentDescription = null, tint = accent)
                }
            }
            Text(
                text = meta?.badge.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                modifier = Modifier.padding(top = Dimens.SpaceSM),
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Dimens.SpaceXS),
            )
            Text(
                text = category.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.SpaceXS),
            )
        }
    }
}
