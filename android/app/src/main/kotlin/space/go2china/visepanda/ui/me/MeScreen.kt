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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.ui.theme.Dimens
import space.go2china.visepanda.data.repository.SyncStatus


/**
 * v0.3.18 profile/settings surface (the bottom-nav restructure's "Me").
 * Deploys real Supabase GoTrue authentication endpoints over Retrofit (Phase 1).
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

    // Automatically close dialog when log in succeeds
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            showLoginDialog = false
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.me_title)) }) },
    ) { innerPadding ->
        MeContent(
            isLoggedIn = state.isLoggedIn,
            userEmail = state.userEmail,
            syncStatus = state.syncStatus,
            activeTripTitle = state.activeTripTitle,
            hasCachedTripData = state.hasCachedTripData,
            languageCode = languageCode,
            onSelectLanguage = onSelectLanguage,
            onLogInClick = { showLoginDialog = true },
            onLogOutClick = { viewModel.logout() },
            contentPadding = innerPadding,
        )
    }

    if (showLoginDialog) {
        LogInDialog(
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onLogin = { email, password -> viewModel.login(email, password) },
            onSignUp = { email, password -> viewModel.signUp(email, password) },
            onDismiss = {
                showLoginDialog = false
                viewModel.clearError()
            }
        )
    }
}

@Composable
private fun MeContent(
    isLoggedIn: Boolean,
    userEmail: String?,
    syncStatus: SyncStatus,
    activeTripTitle: String?,
    hasCachedTripData: Boolean,
    languageCode: String,
    onSelectLanguage: (String) -> Unit,
    onLogInClick: () -> Unit,
    onLogOutClick: () -> Unit,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoggedIn) {
                        Column {
                            Text(
                                text = stringResource(R.string.me_account_logged_in),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = userEmail.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.me_sync_status_label) + ": ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val syncText = when (syncStatus) {
                                    SyncStatus.NOT_SIGNED_IN -> stringResource(R.string.me_sync_status_not_signed_in)
                                    SyncStatus.NOT_SYNCED -> stringResource(R.string.me_sync_status_not_synced)
                                    SyncStatus.SYNCING -> stringResource(R.string.me_sync_status_syncing)
                                    SyncStatus.SYNCED -> stringResource(R.string.me_sync_status_synced)
                                    SyncStatus.FAILED -> stringResource(R.string.me_sync_status_failed)
                                }
                                val syncColor = when (syncStatus) {
                                    SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                                    SyncStatus.SYNCING -> MaterialTheme.colorScheme.secondary
                                    SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Text(
                                    text = syncText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = syncColor
                                )
                            }
                        }
                        TextButton(onClick = onLogOutClick) {
                            Text(
                                text = stringResource(R.string.me_account_log_out),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
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

@Composable
private fun LogInDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isSignUp) {
                    stringResource(R.string.me_login_dialog_signup_tab)
                } else {
                    stringResource(R.string.me_login_dialog_title)
                }
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.me_login_email_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.me_login_password_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                
                Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                
                // Google OAuth is not implemented yet (tracked separately from
                // email/password Phase 1) — disabled and honestly labeled
                // instead of a clickable button that claimed readiness while
                // doing nothing.
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                ) {
                    Text(stringResource(R.string.me_google_signin_coming_soon))
                }

                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                
                // Toggle between Login & SignUp
                TextButton(
                    onClick = { isSignUp = !isSignUp },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (isSignUp) {
                            stringResource(R.string.me_login_switch_to_login)
                        } else {
                            stringResource(R.string.me_login_switch_to_signup)
                        }
                    )
                }
            }
        },
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                TextButton(
                    onClick = {
                        if (isSignUp) {
                            onSignUp(email, password)
                        } else {
                            onLogin(email, password)
                        }
                    },
                    enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading
                ) {
                    Text(
                        text = if (isSignUp) {
                            stringResource(R.string.me_login_dialog_signup_tab)
                        } else {
                            stringResource(R.string.me_login_submit)
                        }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.me_login_cancel))
            }
        },
    )
}
