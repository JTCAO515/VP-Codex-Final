package space.go2china.visepanda.ui.butler

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import space.go2china.visepanda.R
import space.go2china.visepanda.ui.components.EmptyStateView

/**
 * v0.3.3/v0.3.4 placeholder — real content lands in v0.3.5 (Butler + Sync
 * Bridge), which connects this surface to the existing /api/chat pipeline
 * and structured-response contract. This is a real, honest placeholder
 * (not a fake chat UI with canned replies) — see the same "Coming soon"
 * discipline the web app applies to disabled affordances.
 */
@Composable
fun ButlerScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        EmptyStateView(
            title = stringResource(R.string.butler_placeholder_title),
            message = stringResource(R.string.butler_placeholder_body),
            modifier = Modifier.padding(innerPadding),
        )
    }
}
