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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.ui.theme.Dimens

/**
 * New v0.3.8 profile/settings surface (the bottom-nav restructure's "Me").
 * Account editing waits for Supabase auth to be configured; local trip data
 * still appears in the trip section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    languageCode: String,
    onSelectLanguage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showLoginDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.me_title)) }) },
    ) { innerPadding ->
        MeContent(
            activeTripTitle = state.activeTripTitle,
            hasCachedTripData = state.hasCachedTripData,
            languageCode = languageCode,
            onSelectLanguage = onSelectLanguage,
            onLogInClick = { showLoginDialog = true },
            contentPadding = innerPadding,
        )
    }

    if (showLoginDialog) {
        LogInDialog(onDismiss = { showLoginDialog = false })
    }
}

@Composable
private fun MeContent(
    activeTripTitle: String?,
    hasCachedTripData: Boolean,
    languageCode: String,
    onSelectLanguage: (String) -> Unit,
    onLogInClick: () -> Unit,
    contentPadding: PaddingValues,
) {
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
                text = stringResource(R.string.me_account_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        }
        item {
            Text(
                text = stringResource(R.string.me_section_account),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Dimens.SpaceMD),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.me_account_log_in),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    TextButton(onClick = onLogInClick) {
                        Text(stringResource(R.string.me_account_log_in))
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
        }
        item {
            Text(
                text = stringResource(R.string.me_section_language),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            LanguageToggle(languageCode = languageCode, onSelectLanguage = onSelectLanguage)
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
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
            // v0.3.14 Phase 5 — this reads the same Room-backed active-trip
            // cache every other screen uses, so it reports what's actually
            // on-device instead of a permanent placeholder.
            val offlineDataValue = if (hasCachedTripData) {
                stringResource(R.string.me_privacy_offline_cached)
            } else {
                stringResource(R.string.me_privacy_offline_empty)
            }
            SettingsSection(
                title = stringResource(R.string.me_section_privacy),
                rows = listOf(
                    stringResource(R.string.me_privacy_offline_data) to offlineDataValue,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageToggle(languageCode: String, onSelectLanguage: (String) -> Unit) {
    val options = listOf("en" to R.string.me_language_english, "zh-CN" to R.string.me_language_chinese)
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (code, labelRes) ->
            SegmentedButton(
                selected = languageCode == code,
                onClick = { onSelectLanguage(code) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(stringResource(labelRes))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogInDialog(onDismiss: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.me_login_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.me_login_email_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.me_login_password_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                // Honest disclosure per DESIGN.md ADR-118 — this is a real,
                // functional form, but there is no Supabase session behind
                // it yet; submitting must not pretend to log the user in.
                Text(
                    text = stringResource(R.string.me_login_not_connected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = false) {
                Text(stringResource(R.string.me_login_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.me_login_cancel))
            }
        },
    )
}
