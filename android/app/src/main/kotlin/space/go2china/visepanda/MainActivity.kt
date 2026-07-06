package space.go2china.visepanda

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import space.go2china.visepanda.navigation.VisePandaApp
import space.go2china.visepanda.ui.theme.VisePandaTheme

private const val AUTH_CALLBACK_SCHEME = "space.go2china.visepanda"
private const val AUTH_CALLBACK_HOST = "auth-callback"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingGoogleAuthCallback by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            VisePandaTheme {
                VisePandaApp(
                    pendingGoogleAuthCallback = pendingGoogleAuthCallback,
                    onGoogleAuthCallbackConsumed = { pendingGoogleAuthCallback = null },
                )
            }
        }
    }

    // The Custom Tab redirects back into the already-running task instead of
    // creating a new one — singleTop launch mode (AndroidManifest.xml) routes
    // that redirect here rather than through a fresh onCreate.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == AUTH_CALLBACK_SCHEME && uri.host == AUTH_CALLBACK_HOST) {
            pendingGoogleAuthCallback = uri.toString()
        }
    }
}
