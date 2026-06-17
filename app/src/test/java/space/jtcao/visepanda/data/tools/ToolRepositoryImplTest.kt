package space.jtcao.visepanda.data.tools

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRepositoryImplTest {

    @Test
    fun `repository should expose at least six travel help entries`() = runBlocking {
        val result = ToolRepositoryImpl().getEntries()
        assertTrue(result.size >= 6)
    }
}
