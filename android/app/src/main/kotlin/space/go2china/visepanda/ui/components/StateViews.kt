package space.go2china.visepanda.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import space.go2china.visepanda.R
import space.go2china.visepanda.ui.theme.Dimens

/**
 * Every screen skeleton (Today/Plan/Butler-placeholder/Explore-placeholder/
 * Tools-placeholder) renders through one of these four states — this is the
 * v0.3.3 acceptance requirement "static screen skeletons with empty/
 * loading/offline/error placeholders" made concrete as reusable composables
 * instead of four different one-off implementations per screen.
 */

@Composable
fun EmptyStateView(
    title: String = stringResource(R.string.state_empty_title),
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    CenteredMessage(title = title, message = message, modifier = modifier)
}

@Composable
fun LoadingStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(
            modifier = Modifier.padding(top = Dimens.SpaceMD),
        )
        Text(
            text = stringResource(R.string.state_loading),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
    ) {
        Text(
            text = stringResource(R.string.today_offline_banner),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Dimens.SpaceLG, vertical = Dimens.SpaceSM),
        )
    }
}

@Composable
fun ErrorStateView(
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Dimens.SpaceLG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.state_error_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Dimens.SpaceSM),
            )
        }
        if (onRetry != null) {
            Button(onClick = onRetry, modifier = Modifier.padding(top = Dimens.SpaceMD)) {
                Text(stringResource(R.string.state_error_retry))
            }
        }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    message: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Dimens.SpaceLG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Dimens.SpaceSM),
            )
        }
    }
}
