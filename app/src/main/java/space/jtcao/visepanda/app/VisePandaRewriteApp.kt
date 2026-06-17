package space.jtcao.visepanda.app

import androidx.compose.runtime.Composable
import space.jtcao.visepanda.core.designsystem.VpTheme
import space.jtcao.visepanda.feature.navigation.RewriteNavHost

@Composable
fun VisePandaRewriteApp() {
    VpTheme {
        RewriteNavHost()
    }
}
