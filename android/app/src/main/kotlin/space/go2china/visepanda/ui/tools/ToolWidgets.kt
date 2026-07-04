package space.go2china.visepanda.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import java.text.NumberFormat
import java.util.Locale
import space.go2china.visepanda.data.model.PaymentCardBrand
import space.go2china.visepanda.data.model.PaymentWallet
import space.go2china.visepanda.data.model.ToolInteractiveDescriptor
import space.go2china.visepanda.data.model.VisaNationality
import space.go2china.visepanda.ui.theme.Dimens

@Composable
fun ToolInteractiveWidget(descriptor: ToolInteractiveDescriptor, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(Dimens.RadiusLG),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceLG)) {
            when (descriptor) {
                is ToolInteractiveDescriptor.CurrencyConverter -> CurrencyConverterWidget(descriptor)
                is ToolInteractiveDescriptor.VisaChecker -> VisaCheckerWidget(descriptor)
                is ToolInteractiveDescriptor.PaymentWizard -> PaymentWizardWidget(descriptor)
            }
        }
    }
}

@Composable
private fun WidgetHeader(title: String, badge: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(text = badge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
    }
    Spacer(modifier = Modifier.height(Dimens.SpaceMD))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyConverterWidget(descriptor: ToolInteractiveDescriptor.CurrencyConverter) {
    var amountText by remember { mutableStateOf("100") }
    var target by remember { mutableStateOf(descriptor.defaultTarget) }
    var expanded by remember { mutableStateOf(false) }

    WidgetHeader(
        title = "Quick RMB converter",
        badge = if (descriptor.ratesAreLive) "Live rate" else "Live rate required",
    )

    if (!descriptor.ratesAreLive || descriptor.rates.isEmpty()) {
        Text(
            text = "Live exchange rates are unavailable. Configure EXCHANGE_RATE_API_KEY before using the converter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
    ) {
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
            label = { Text("RMB amount") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(Dimens.RadiusMD),
            modifier = Modifier.weight(1f),
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f),
        ) {
            OutlinedTextField(
                value = target,
                onValueChange = {},
                readOnly = true,
                label = { Text("Convert to") },
                shape = RoundedCornerShape(Dimens.RadiusMD),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                descriptor.rates.keys.forEach { code ->
                    DropdownMenuItem(
                        text = { Text(code) },
                        onClick = { target = code; expanded = false },
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val rate = descriptor.rates[target] ?: 0.0
    val converted = amount * rate
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 } }
    Text(
        text = "¥${formatter.format(amount)} ≈ ${formatter.format(converted)} $target",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
        descriptor.commonAmounts.forEach { quickAmount ->
            AssistChip(onClick = { amountText = quickAmount.toString() }, label = { Text("¥$quickAmount") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisaCheckerWidget(descriptor: ToolInteractiveDescriptor.VisaChecker) {
    var selected by remember { mutableStateOf(descriptor.nationalities.first()) }
    var daysText by remember { mutableStateOf("7") }
    var transitOnly by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    WidgetHeader(title = "Entry planning check", badge = "Conservative guidance")

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Passport nationality") },
            shape = RoundedCornerShape(Dimens.RadiusMD),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            descriptor.nationalities.forEach { nationality ->
                DropdownMenuItem(
                    text = { Text(nationality.label) },
                    onClick = { selected = nationality; expanded = false },
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

    OutlinedTextField(
        value = daysText,
        onValueChange = { daysText = it.filter(Char::isDigit) },
        label = { Text("Days in mainland China") },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(Dimens.RadiusMD),
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = transitOnly, onCheckedChange = { transitOnly = it })
        Text(
            text = "Transit-only route with onward travel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

    val days = daysText.toIntOrNull() ?: 0
    val status = visaStatusFor(selected, days, transitOnly)
    Text(text = status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(Dimens.SpaceXS))
    Text(text = selected.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun visaStatusFor(nationality: VisaNationality, days: Int, transitOnly: Boolean): String = when {
    transitOnly && nationality.transitHours != null ->
        "May fit a transit-without-visa route up to ${nationality.transitHours} hours if the itinerary qualifies."
    nationality.visaFreeDays != null && days > nationality.visaFreeDays ->
        "Your $days-day stay is longer than the ${nationality.visaFreeDays}-day planning threshold shown here. Plan for a visa check."
    nationality.visaFreeDays != null && days <= nationality.visaFreeDays ->
        "Likely short-stay visa-free for up to $days days, but confirm current official rules."
    else -> "Confirm with an official embassy or consulate before booking."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentWizardWidget(descriptor: ToolInteractiveDescriptor.PaymentWizard) {
    var wallet by remember { mutableStateOf(descriptor.wallets.first()) }
    var card by remember { mutableStateOf(descriptor.cardBrands.first()) }
    var walletExpanded by remember { mutableStateOf(false) }
    var cardExpanded by remember { mutableStateOf(false) }

    WidgetHeader(title = "Payment setup path", badge = "Pre-trip")

    ExposedDropdownMenuBox(expanded = walletExpanded, onExpandedChange = { walletExpanded = it }) {
        OutlinedTextField(
            value = wallet.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Wallet") },
            shape = RoundedCornerShape(Dimens.RadiusMD),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        DropdownMenu(expanded = walletExpanded, onDismissRequest = { walletExpanded = false }) {
            descriptor.wallets.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { wallet = option; walletExpanded = false },
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

    ExposedDropdownMenuBox(expanded = cardExpanded, onExpandedChange = { cardExpanded = it }) {
        OutlinedTextField(
            value = card.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Card brand") },
            shape = RoundedCornerShape(Dimens.RadiusMD),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        DropdownMenu(expanded = cardExpanded, onDismissRequest = { cardExpanded = false }) {
            descriptor.cardBrands.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { card = option; cardExpanded = false },
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

    val steps = paymentStepsFor(wallet, card)
    steps.forEachIndexed { index, step ->
        Text(
            text = "${index + 1}. $step",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = Dimens.SpaceXS),
        )
    }

    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
    Text(text = card.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun paymentStepsFor(wallet: PaymentWallet, card: PaymentCardBrand): List<String> = listOf(
    "Install ${wallet.appName} before departure and create your account on Wi-Fi.",
    "Add your ${card.label} and complete issuer verification while your home phone number still works.",
    "Save your passport name spelling exactly as it appears on your travel document.",
    "Carry a small RMB cash backup and one physical card in a separate place.",
)
