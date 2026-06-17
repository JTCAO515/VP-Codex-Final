package com.visepanda.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.*
import com.visepanda.core.designsystem.component.VpPrimaryButton
import com.visepanda.core.designsystem.component.VpSecondaryButton
import com.visepanda.core.designsystem.spacing.VpSpacing

@Composable
fun AuthScreen(onBack: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(VpSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.displayLarge,
            color = GoldPrimary,
        )
        Spacer(modifier = Modifier.height(VpSpacing.xxl))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = Divider,
                cursorColor = GoldPrimary,
            ),
        )
        Spacer(modifier = Modifier.height(VpSpacing.md))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = Divider,
                cursorColor = GoldPrimary,
            ),
        )
        Spacer(modifier = Modifier.height(VpSpacing.xl))

        VpPrimaryButton(text = "Log In", onClick = {})
        Spacer(modifier = Modifier.height(VpSpacing.sm))
        VpSecondaryButton(text = "Create Account", onClick = {})
    }
}
