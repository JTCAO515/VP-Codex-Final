package space.jtcao.visepanda.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader
import space.jtcao.visepanda.data.auth.AuthDependencies
import space.jtcao.visepanda.domain.usecase.LoginWithEmailUseCase
import space.jtcao.visepanda.domain.usecase.ObserveSessionUseCase
import space.jtcao.visepanda.domain.usecase.RegisterWithEmailUseCase

@Composable
fun LoginScreen(
    onOpenRegister: () -> Unit,
    onOpenForgotPassword: () -> Unit,
    onAuthSuccess: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = authViewModelFactory())
) {
    var email by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var password by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.user?.id) {
        if (uiState.user != null) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VpSectionHeader(
            title = "Login",
            subtitle = "Use your email account to continue with the rewrite experience."
        )

        Text(
            text = "Email auth is routed now. Backend session wiring is still handled by the auth repository flow.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        uiState.error?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = { viewModel.login(email.trim(), password) },
            enabled = !uiState.loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.loading) "Signing in..." else "Login")
        }

        OutlinedButton(
            onClick = onOpenRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create account")
        }

        TextButton(
            onClick = onOpenForgotPassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forgot password")
        }

        TextButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to home")
        }
    }
}

internal fun authViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = AuthDependencies.repository
        return AuthViewModel(
            loginWithEmail = LoginWithEmailUseCase(repository),
            registerWithEmail = RegisterWithEmailUseCase(repository),
            observeSession = ObserveSessionUseCase(repository),
            resetPasswordAction = repository::resetPassword,
            updatePasswordAction = repository::updatePassword,
            logoutAction = repository::logout
        ) as T
    }
}
