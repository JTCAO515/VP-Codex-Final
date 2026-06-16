package com.visepanda.hermes.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.BorderDefault
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.GoldLight
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.TextTertiary
import com.visepanda.designsystem.components.VpGoldButton
import com.visepanda.designsystem.components.VpEnterAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Screen mode ──

enum class AuthMode { LOGIN, REGISTER }

// ── Auth State ──

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

// ── AuthScreen ──

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var uiState by remember { mutableStateOf(AuthUiState()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { com.visepanda.hermes.data.AuthRepository(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            VpEnterAnimation(index = 0) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldLight, Gold)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            VpEnterAnimation(index = 1) {
                Text(
                    text = if (mode == AuthMode.LOGIN) "Welcome to VisePanda" else "Create Account",
                    style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            VpEnterAnimation(index = 2) {
                Text(
                    text = if (mode == AuthMode.LOGIN)
                        "Sign in to start your China travel journey"
                    else
                        "Join VisePanda and plan your trip",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Error message
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = uiState.error ?: "",
                    color = Color(0xFFD9534F),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Email field
            VpEnterAnimation(index = 3) {
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    enabled = !uiState.isLoading
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            VpEnterAnimation(index = 4) {
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    keyboardType = KeyboardType.Password,
                    imeAction = if (mode == AuthMode.REGISTER) ImeAction.Next else ImeAction.Done,
                    isPassword = true,
                    enabled = !uiState.isLoading,
                    onImeAction = {
                        if (mode == AuthMode.LOGIN) {
                            scope.launch {
                                handleAuth(
                                    mode, email, password, "",
                                    repo, onAuthSuccess,
                                    onStateUpdate = { uiState = it }
                                )
                            }
                        }
                    }
                )
            }

            // Confirm password (register only)
            AnimatedVisibility(visible = mode == AuthMode.REGISTER) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        enabled = !uiState.isLoading,
                        onImeAction = {
                            scope.launch {
                                handleAuth(
                                    mode, email, password, confirmPassword,
                                    repo, onAuthSuccess,
                                    onStateUpdate = { uiState = it }
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            VpEnterAnimation(index = 5) {
                VpGoldButton(
                    text = if (mode == AuthMode.LOGIN) "Sign In" else "Create Account",
                    onClick = {
                        scope.launch {
                            handleAuth(
                                mode, email, password, confirmPassword,
                                repo, onAuthSuccess,
                                onStateUpdate = { uiState = it }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle mode
            VpEnterAnimation(index = 6) {
                RowText(
                    text = if (mode == AuthMode.LOGIN)
                        "Don't have an account? "
                    else
                        "Already have an account? ",
                    action = if (mode == AuthMode.LOGIN) "Create One" else "Sign In",
                    enabled = !uiState.isLoading,
                    onClick = {
                        mode = if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                        uiState = AuthUiState()
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    onImeAction: () -> Unit = {}
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(Surface)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = TextTertiary,
                fontSize = 15.sp
            )
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TextPrimary,
                fontSize = 15.sp
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            singleLine = true,
            enabled = enabled,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Gold)
        )
    }
}

@Composable
private fun RowText(
    text: String,
    action: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = action,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = if (enabled) Gold else TextTertiary,
            modifier = Modifier.clickable(enabled = enabled) { onClick() }
        )
    }
}

// ── Auth logic ──

private suspend fun handleAuth(
    mode: AuthMode,
    email: String,
    password: String,
    confirmPassword: String,
    repo: com.visepanda.hermes.data.AuthRepository,
    onSuccess: () -> Unit,
    onStateUpdate: (AuthUiState) -> Unit
) {
    val trimmedEmail = email.trim().lowercase()

    // Validation
    if (trimmedEmail.isBlank() || password.isBlank()) {
        onStateUpdate(AuthUiState(error = "Email and password required"))
        return
    }
    if ("@" !in trimmedEmail || "." !in trimmedEmail.substringAfter("@")) {
        onStateUpdate(AuthUiState(error = "Please enter a valid email"))
        return
    }
    if (password.length < 6) {
        onStateUpdate(AuthUiState(error = "Password must be at least 6 characters"))
        return
    }
    if (mode == AuthMode.REGISTER && password != confirmPassword) {
        onStateUpdate(AuthUiState(error = "Passwords do not match"))
        return
    }

    onStateUpdate(AuthUiState(isLoading = true))

    val result = if (mode == AuthMode.LOGIN) {
        repo.login(trimmedEmail, password)
    } else {
        repo.register(trimmedEmail, password)
    }

    result.fold(
        onSuccess = { responseJson ->
            val token = com.visepanda.hermes.data.AuthRepository.parseToken(responseJson)
            if (token != null) {
                repo.saveToken(token)
            }
            val user = com.visepanda.hermes.data.AuthRepository.parseUser(responseJson)
            if (user != null) {
                repo.saveUser(user)
            }
            onStateUpdate(AuthUiState(isSuccess = true))
            delay(200)
            onSuccess()
        },
        onFailure = { e ->
            onStateUpdate(AuthUiState(error = e.message ?: "Something went wrong"))
        }
    )
}
