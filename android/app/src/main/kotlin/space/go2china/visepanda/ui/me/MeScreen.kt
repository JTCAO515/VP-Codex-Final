package space.go2china.visepanda.ui.me

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
    // v0.3.x (Issue #67): the profile card is now the single entry point into
    // account state — tapping it opens this sheet, which shows either the
    // login form or signed-in info + sign out, instead of a form/row that was
    // always rendered inline on the page (mirrors iOS PR #66's AuthSheetView).
    var showAccountSheet by remember { mutableStateOf(false) }
    var purchaseNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            showAccountSheet = false
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
            onAccountCardClick = { showAccountSheet = true },
            onSubscribeClick = { plan -> purchaseNotice = plan },
            onLegalPlaceholderClick = { label -> purchaseNotice = label },
            contentPadding = innerPadding,
        )
    }

    if (showAccountSheet) {
        ModalBottomSheet(onDismissRequest = { showAccountSheet = false }) {
            AccountSheetContent(
                isLoggedIn = state.isLoggedIn,
                userEmail = state.userEmail,
                isLoading = state.isLoading,
                errorMessage = state.errorMessage,
                onLogin = { email, password -> viewModel.login(email, password) },
                onSignUp = { email, password -> viewModel.signUp(email, password) },
                onLogOutClick = {
                    viewModel.logout()
                    showAccountSheet = false
                },
                onClearError = viewModel::clearError,
            )
        }
    }

    // Subscribe/Restore/Terms/Privacy are all placeholders pending real Google
    // Play Billing wiring (tracked separately) — see subscriptionSection below.
    purchaseNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { purchaseNotice = null },
            title = { Text(stringResource(R.string.me_subscription_placeholder_title)) },
            text = { Text(notice) },
            confirmButton = {
                TextButton(onClick = { purchaseNotice = null }) { Text("OK") }
            },
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
    onAccountCardClick: () -> Unit,
    onSubscribeClick: (String) -> Unit,
    onLegalPlaceholderClick: (String) -> Unit,
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
            // v0.3.x (Issue #67): the whole card is now the tap target that
            // opens the account sheet — it only ever shows a compact preview,
            // never the full login form or sign-out control inline.
            Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onAccountCardClick)) {
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
                    } else {
                        Text(
                            text = stringResource(R.string.me_account_log_in),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
        }
        item {
            SubscriptionSection(onSubscribeClick = onSubscribeClick, onLegalPlaceholderClick = onLegalPlaceholderClick)
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

/**
 * Subscription placeholder (Issue #67, mirrors iOS PR #66): two priced tiers,
 * Subscribe only ever shows a placeholder notice — Google Play Billing is a
 * separate SDK/flow not wired up yet. The legal/management entries below are
 * required to ship even as placeholders (Restore purchase, auto-renewal
 * disclosure, Play account management, Terms of Use, Privacy Policy) so the
 * surface reads as honest rather than a bare "buy" button with no recourse.
 */
@Composable
private fun SubscriptionSection(
    onSubscribeClick: (String) -> Unit,
    onLegalPlaceholderClick: (String) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.me_section_subscription),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
            SubscriptionPlanCard(
                title = stringResource(R.string.me_subscription_human_title),
                price = stringResource(R.string.me_subscription_human_price),
                summary = stringResource(R.string.me_subscription_human_summary),
                onSubscribe = {
                    onSubscribeClick("StoreKit purchase placeholder. Product id: visepanda.human.monthly")
                },
            )
            SubscriptionPlanCard(
                title = stringResource(R.string.me_subscription_premium_title),
                price = stringResource(R.string.me_subscription_premium_price),
                summary = stringResource(R.string.me_subscription_premium_summary),
                onSubscribe = {
                    onSubscribeClick("StoreKit purchase placeholder. Product id: visepanda.premium.monthly")
                },
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
                    TextButton(
                        onClick = {
                            onLegalPlaceholderClick("Restore purchases placeholder. Google Play Billing restore will be connected with real products.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.me_subscription_restore))
                    }
                    Text(
                        text = stringResource(R.string.me_subscription_renewal_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens.SpaceXS),
                    )
                    Row(modifier = Modifier.padding(top = Dimens.SpaceSM)) {
                        TextButton(onClick = { onLegalPlaceholderClick("Terms of Use placeholder.") }) {
                            Text(stringResource(R.string.me_subscription_terms))
                        }
                        TextButton(onClick = { onLegalPlaceholderClick("Privacy Policy placeholder.") }) {
                            Text(stringResource(R.string.me_subscription_privacy))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(title: String, price: String, summary: String, onSubscribe: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(price, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.SpaceXS, bottom = Dimens.SpaceSM),
            )
            Button(onClick = onSubscribe, colors = ButtonDefaults.buttonColors(), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.me_subscription_subscribe))
            }
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

/**
 * v0.3.x (Issue #67): the account sheet opened from the Me profile card.
 * Mirrors iOS PR #66's AuthSheetView — one sheet handles both states rather
 * than an always-inline form (signed-out) or an always-inline row with a
 * sign-out button (signed-in).
 */
@Composable
private fun AccountSheetContent(
    isLoggedIn: Boolean,
    userEmail: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onLogOutClick: () -> Unit,
    onClearError: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(Dimens.SpaceLG)) {
        if (isLoggedIn) {
            Text(
                text = stringResource(R.string.me_account_logged_in),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = userEmail.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceLG))
            Button(
                onClick = onLogOutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            ) {
                Text(stringResource(R.string.me_account_log_out))
            }
        } else {
            LogInForm(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLogin = onLogin,
                onSignUp = onSignUp,
                onClearError = onClearError,
            )
        }
        Spacer(modifier = Modifier.height(Dimens.SpaceLG))
    }
}

@Composable
private fun ColumnScope.LogInForm(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onClearError: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Text(
        text = if (isSignUp) stringResource(R.string.me_login_dialog_signup_tab) else stringResource(R.string.me_login_dialog_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(Dimens.SpaceMD))
    OutlinedTextField(
        value = email,
        onValueChange = {
            email = it
            onClearError()
        },
        label = { Text(stringResource(R.string.me_login_email_label)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isLoading
    )
    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            onClearError()
        },
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

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 8.dp), strokeWidth = 2.dp)
        }
        Button(
            onClick = { if (isSignUp) onSignUp(email, password) else onLogin(email, password) },
            enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading,
        ) {
            Text(
                text = if (isSignUp) stringResource(R.string.me_login_dialog_signup_tab) else stringResource(R.string.me_login_submit),
            )
        }
    }

    Spacer(modifier = Modifier.height(Dimens.SpaceSM))

    // Google OAuth is not implemented yet (tracked separately from
    // email/password Phase 1) — disabled and honestly labeled instead of a
    // clickable button that claimed readiness while doing nothing.
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
        Text(stringResource(R.string.me_google_signin_coming_soon))
    }

    Spacer(modifier = Modifier.height(Dimens.SpaceSM))

    TextButton(
        onClick = { isSignUp = !isSignUp },
        modifier = Modifier.align(Alignment.CenterHorizontally),
        enabled = !isLoading,
    ) {
        Text(
            text = if (isSignUp) stringResource(R.string.me_login_switch_to_login) else stringResource(R.string.me_login_switch_to_signup),
        )
    }
}
