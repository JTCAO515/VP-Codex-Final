package space.go2china.visepanda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import space.go2china.visepanda.navigation.VisePandaApp
import space.go2china.visepanda.ui.theme.VisePandaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisePandaTheme {
                VisePandaApp()
            }
        }
    }
}
