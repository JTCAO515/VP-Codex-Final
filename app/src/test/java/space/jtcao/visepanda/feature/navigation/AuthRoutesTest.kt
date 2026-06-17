package space.jtcao.visepanda.feature.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRoutesTest {

    @Test
    fun `auth routes should stay stable`() {
        assertEquals("account", RewriteRoutes.ACCOUNT)
        assertEquals("login", RewriteRoutes.LOGIN)
        assertEquals("register", RewriteRoutes.REGISTER)
        assertEquals("forgot-password", RewriteRoutes.FORGOT_PASSWORD)
        assertEquals("reset-password", RewriteRoutes.RESET_PASSWORD)
    }
}
