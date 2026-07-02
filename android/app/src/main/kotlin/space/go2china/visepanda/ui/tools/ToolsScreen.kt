package space.go2china.visepanda.ui.tools

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import space.go2china.visepanda.R
import space.go2china.visepanda.ui.components.EmptyStateView

/** v0.3.3/v0.3.4 placeholder — real content lands in v0.3.6 (Native Translator Utility). */
@Composable
fun ToolsScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        EmptyStateView(
            title = stringResource(R.string.tools_placeholder_title),
            message = stringResource(R.string.tools_placeholder_body),
            modifier = Modifier.padding(innerPadding),
        )
    }
}
