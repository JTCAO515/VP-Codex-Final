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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.jtcao.visepanda.core.designsystem.components.VpSectionHeader

@Composable
fun ResetPasswordScreen(
    onOpenLogin: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = authViewModelFactory())
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var localMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var attemptedUpdate by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VpSectionHeader(
            title = "Set New Password",
            subtitle = "Use the recovery session from your email link to finish the password reset flow."
        )

        Text(
            text = "Choose a new password after the recovery link opens the app.",
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

        localMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val updateSucceeded = attemptedUpdate && !uiState.loading && uiState.error == null && localMessage == null
        if (updateSucceeded) {
            Text(
                text = "Password updated. You can now sign in with the new password.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                localMessage = null
                attemptedUpdate = false
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("New password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localMessage = null
                attemptedUpdate = false
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = {
                localMessage = when {
                    password.isBlank() -> "Password is required."
                    confirmPassword.isBlank() -> "Please confirm your password."
                    password != confirmPassword -> "Passwords do not match."
                    else -> {
                        attemptedUpdate = true
                        viewModel.updatePassword(password)
                        null
                    }
                }
            },
            enabled = !uiState.loading && password.isNotBlank() && confirmPassword.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.loading) "Updating..." else "Update password")
        }

        OutlinedButton(
            onClick = onOpenLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (updateSucceeded) "Go to login" else "Back to login")
        }

        OutlinedButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to home")
        }
    }
}
