package space.go2china.visepanda.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * v0.3.14: applies the Me screen's language preference (/goal Phase 3)
 * across the whole composition without adding an AppCompat dependency —
 * overrides [LocalConfiguration] and [LocalContext]'s resources so every
 * `stringResource()` call below this point resolves against
 * `values-zh-rCN` (or the default `values`) accordingly. Content that
 * isn't backed by a string resource (hardcoded English copy in Compose
 * code, mock data) is not translated by this — see DESIGN.md ADR-118 for
 * the honest scope of what this actually localizes.
 *
 * [LocaleContextWrapper] stays a real [ContextWrapper] around the
 * Activity context (only overriding [getResources]) instead of swapping in
 * the plain `Context` that [Context.createConfigurationContext] returns —
 * that plain context isn't an `Activity` and isn't wrapped around one, so
 * Hilt's `hiltViewModel()` (which walks `ContextWrapper.baseContext` to
 * find the owning `Activity`) crashed with "Expected an activity context
 * for creating a HiltViewModelFactory" the first time this was tried
 * without preserving the wrapper chain.
 */
@Composable
fun LocalizedApp(languageCode: String, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val locale = if (languageCode.isBlank() || languageCode == "en") Locale.ENGLISH else Locale.forLanguageTag(languageCode)

    val configuration = remember(languageCode) { Configuration(baseConfiguration) }
    configuration.setLocale(locale)
    val localizedContext = remember(baseContext, languageCode) {
        LocaleContextWrapper(baseContext, configuration)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
    ) {
        content()
    }
}

private class LocaleContextWrapper(base: Context, configuration: Configuration) : ContextWrapper(base) {
    private val localizedResources: Resources = base.createConfigurationContext(configuration).resources

    override fun getResources(): Resources = localizedResources
}
