package space.jtcao.visepanda.feature.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import space.jtcao.visepanda.domain.model.AuthUser
import space.jtcao.visepanda.domain.repository.AuthRepository
import space.jtcao.visepanda.domain.usecase.LoginWithEmailUseCase
import space.jtcao.visepanda.domain.usecase.ObserveSessionUseCase
import space.jtcao.visepanda.domain.usecase.RegisterWithEmailUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success should expose authenticated user`() = runTest {
        val repo = fakeRepository(
            loginResult = Result.success(
                AuthUser("1", "user@test.com", "Alice", "user", "active")
            )
        )

        val viewModel = AuthViewModel(
            loginWithEmail = LoginWithEmailUseCase(repo),
            registerWithEmail = RegisterWithEmailUseCase(repo),
            observeSession = ObserveSessionUseCase(repo)
        )

        viewModel.login("user@test.com", "secret123")
        advanceUntilIdle()

        assertEquals("user@test.com", viewModel.uiState.value.user?.email)
        assertFalse(viewModel.uiState.value.loading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `register success should expose pending user`() = runTest {
        val repo = fakeRepository(
            registerResult = Result.success(
                AuthUser("2", "new@test.com", "New User", "user", "pending")
            )
        )

        val viewModel = AuthViewModel(
            loginWithEmail = LoginWithEmailUseCase(repo),
            registerWithEmail = RegisterWithEmailUseCase(repo),
            observeSession = ObserveSessionUseCase(repo)
        )

        viewModel.register("new@test.com", "secret123", "New User")
        advanceUntilIdle()

        assertEquals("new@test.com", viewModel.uiState.value.user?.email)
        assertEquals("pending", viewModel.uiState.value.user?.status)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `session observation should expose existing user on init`() = runTest {
        val sessionUser = AuthUser("3", "session@test.com", "Session User", "admin", "active")
        val repo = fakeRepository(
            sessionFlow = flowOf(sessionUser)
        )

        val viewModel = AuthViewModel(
            loginWithEmail = LoginWithEmailUseCase(repo),
            registerWithEmail = RegisterWithEmailUseCase(repo),
            observeSession = ObserveSessionUseCase(repo)
        )

        advanceUntilIdle()

        assertEquals("session@test.com", viewModel.uiState.value.user?.email)
        assertEquals("admin", viewModel.uiState.value.user?.role)
    }

    @Test
    fun `reset password success should clear error`() = runTest {
        val repo = fakeRepository(
            loginResult = Result.failure(IllegalStateException("wrong password")),
            resetPasswordResult = Result.success(Unit)
        )

        val viewModel = AuthViewModel(
            loginWithEmail = LoginWithEmailUseCase(repo),
            registerWithEmail = RegisterWithEmailUseCase(repo),
            observeSession = ObserveSessionUseCase(repo),
            resetPasswordAction = repo::resetPassword
        )

        viewModel.login("user@test.com", "wrong-password")
        advanceUntilIdle()
        assertEquals("wrong password", viewModel.uiState.value.error)

        viewModel.resetPassword("user@test.com")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `update password success should clear error`() = runTest {
        val repo = fakeRepository(
            loginResult = Result.failure(IllegalStateException("expired session")),
            updatePasswordResult = Result.success(Unit)
        )

        val viewModel = AuthViewModel(
            loginWithEmail = LoginWithEmailUseCase(repo),
            registerWithEmail = RegisterWithEmailUseCase(repo),
            observeSession = ObserveSessionUseCase(repo),
            updatePasswordAction = repo::updatePassword
        )

        viewModel.login("user@test.com", "wrong-password")
        advanceUntilIdle()
        assertEquals("expired session", viewModel.uiState.value.error)

        viewModel.updatePassword("new-password-123")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `logout should clear authenticated user`() = runTest {
        val repo = fakeRepository(
            sessionFlow = flowOf(
                AuthUser("1", "user@test.com", "Alice", "user", "active")
            )
        )

        val viewModel = AuthViewModel(
            loginWithEmail = LoginWithEmailUseCase(repo),
            registerWithEmail = RegisterWithEmailUseCase(repo),
            observeSession = ObserveSessionUseCase(repo),
            logoutAction = repo::logout
        )

        advanceUntilIdle()
        assertEquals("user@test.com", viewModel.uiState.value.user?.email)

        viewModel.logout()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.user)
        assertFalse(viewModel.uiState.value.loading)
    }

    private fun fakeRepository(
        loginResult: Result<AuthUser> = Result.failure(IllegalStateException("login not configured")),
        registerResult: Result<AuthUser> = Result.failure(IllegalStateException("register not configured")),
        sessionFlow: Flow<AuthUser?> = flowOf(null),
        resetPasswordResult: Result<Unit> = Result.success(Unit),
        updatePasswordResult: Result<Unit> = Result.success(Unit)
    ): AuthRepository = object : AuthRepository {
        override suspend fun login(email: String, password: String): Result<AuthUser> = loginResult

        override suspend fun register(
            email: String,
            password: String,
            displayName: String?
        ): Result<AuthUser> = registerResult

        override fun observeSession(): Flow<AuthUser?> = sessionFlow

        override suspend fun logout() = Unit

        override suspend fun resetPassword(email: String): Result<Unit> = resetPasswordResult

        override suspend fun updatePassword(password: String): Result<Unit> = updatePasswordResult
    }
}
