package space.go2china.visepanda.ui.tools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.data.model.ToolCategory
import space.go2china.visepanda.data.model.ToolSection
import space.go2china.visepanda.ui.components.EmptyStateView
import space.go2china.visepanda.ui.components.LoadingStateView
import space.go2china.visepanda.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolCategoryDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ToolCategoryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = (state as? ToolCategoryDetailUiState.Content)?.category?.name ?: ""
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.tool_category_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val current = state) {
            is ToolCategoryDetailUiState.Loading -> LoadingStateView(modifier = Modifier.padding(innerPadding))
            is ToolCategoryDetailUiState.NotFound -> EmptyStateView(modifier = Modifier.padding(innerPadding))
            is ToolCategoryDetailUiState.Content -> ToolCategoryDetailContent(
                category = current.category,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun ToolCategoryDetailContent(category: ToolCategory, contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(
            start = Dimens.SpaceLG,
            end = Dimens.SpaceLG,
            top = Dimens.SpaceLG,
            bottom = Dimens.BottomNavContentClearance,
        ),
    ) {
        item {
            Text(
                text = category.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        }

        category.interactive?.let { descriptor ->
            item {
                ToolInteractiveWidget(descriptor)
                Spacer(modifier = Modifier.height(Dimens.SpaceLG))
            }
        }

        item {
            Text(
                text = "Tips",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            category.tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Dimens.SpaceXS),
                )
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
        }

        items(category.sections) { section ->
            ToolSectionCard(section)
            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
        }

        item {
            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            OfflineNotesCard(category.offlineTips)
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
            Text(
                text = category.apiPriority,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ToolSectionCard(section: ToolSection) {
    Card(
        shape = RoundedCornerShape(Dimens.RadiusMD),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            section.items.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.SpaceXS),
                )
            }
        }
    }
}

@Composable
private fun OfflineNotesCard(offlineTips: List<String>) {
    Card(
        shape = RoundedCornerShape(Dimens.RadiusMD),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Text(
                text = "Offline notes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            offlineTips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(bottom = Dimens.SpaceXS),
                )
            }
        }
    }
}
