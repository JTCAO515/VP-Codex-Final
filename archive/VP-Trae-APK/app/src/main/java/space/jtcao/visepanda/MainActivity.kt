package space.jtcao.visepanda

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.jan.supabase.auth.handleDeeplinks
import space.jtcao.visepanda.app.VisePandaRewriteApp
import space.jtcao.visepanda.data.auth.SupabaseClientFactory
import space.jtcao.visepanda.data.auth.SupabaseRecoveryCoordinator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSupabaseAuthDeeplink(intent)
        setContent {
            VisePandaRewriteApp()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSupabaseAuthDeeplink(intent)
    }

    private fun handleSupabaseAuthDeeplink(intent: Intent?) {
        intent ?: return
        SupabaseRecoveryCoordinator.onIntent(intent)
        SupabaseClientFactory.createOrNull()?.handleDeeplinks(intent)
    }
}
