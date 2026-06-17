package com.visepanda.feature.city

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.visepanda.core.designsystem.color.*
import com.visepanda.core.designsystem.component.VpPrimaryButton
import com.visepanda.core.designsystem.spacing.VpSpacing

@Composable
fun CityDetailScreen(
    cityId: String,
    onPlanClick: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(VpSpacing.lg),
    ) {
        Text(
            text = cityId.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(VpSpacing.lg))
        Text(
            text = "Discover what makes this city special.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
        )
        Spacer(modifier = Modifier.height(VpSpacing.xl))
        VpPrimaryButton(text = "Plan my trip to $cityId", onClick = onPlanClick)
    }
}
