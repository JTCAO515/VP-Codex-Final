package space.jtcao.visepanda.core.designsystem

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class VpThemeSmokeTest {

    @Test
    fun `gold accent should stay stable`() {
        assertEquals(Color(0xFFC9A45C), VpGold)
    }
}
