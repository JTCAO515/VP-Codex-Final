package space.go2china.visepanda.ui.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import space.go2china.visepanda.ui.theme.Dimens

/**
 * New v0.3.8 profile/settings surface (the bottom-nav restructure's "Me").
 * Only the active-trip row is real data; the rest is honest placeholder
 * content clearly disclosed at the top of the screen — there is no
 * Supabase auth/account system wired up yet. See DESIGN.md ADR-111.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    modifier: Modifier = Modifier,
    viewModel: MeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.me_title)) }) },
    ) { innerPadding ->
        MeContent(activeTripTitle = state.activeTripTitle, contentPadding = innerPadding)
    }
}

@Composable
private fun MeContent(activeTripTitle: String?, contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(Dimens.SpaceLG),
    ) {
        item {
            Text(
                text = stringResource(R.string.me_placeholder_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        }
        item {
            SettingsSection(
                title = stringResource(R.string.me_section_preferences),
                rows = listOf(
                    stringResource(R.string.me_pref_dietary) to stringResource(R.string.me_value_not_set),
                    stringResource(R.string.me_pref_budget) to stringResource(R.string.me_value_not_set),
                    stringResource(R.string.me_pref_crowd) to stringResource(R.string.me_value_not_set),
                ),
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
        }
        item {
            SettingsSection(
                title = stringResource(R.string.me_section_trips),
                rows = listOfNotNull(
                    activeTripTitle?.let { it to stringResource(R.string.me_trip_active) },
                ),
                emptyMessage = stringResource(R.string.me_trips_empty),
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
        }
        item {
            SettingsSection(
                title = stringResource(R.string.me_section_privacy),
                rows = listOf(
                    stringResource(R.string.me_privacy_offline_data) to stringResource(R.string.me_value_not_available),
                ),
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, rows: List<Pair<String, String>>, emptyMessage: String? = null) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                if (rows.isEmpty() && emptyMessage != null) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Dimens.SpaceMD),
                    )
                }
                rows.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpaceMD, vertical = Dimens.SpaceMD),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
