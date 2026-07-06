package space.jtcao.visepanda.data.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseRecoveryCoordinatorTest {

    @Test
    fun `fragment recovery link should be detected`() {
        assertTrue(
            SupabaseRecoveryCoordinator.isRecoveryUrl(
                "space.jtcao.visepanda://auth-callback#access_token=token&type=recovery"
            )
        )
    }

    @Test
    fun `query recovery link should be detected`() {
        assertTrue(
            SupabaseRecoveryCoordinator.isRecoveryUrl(
                "space.jtcao.visepanda://auth-callback?type=recovery"
            )
        )
    }

    @Test
    fun `non recovery link should be ignored`() {
        assertFalse(
            SupabaseRecoveryCoordinator.isRecoveryUrl(
                "space.jtcao.visepanda://auth-callback#type=signup"
            )
        )
    }
}
