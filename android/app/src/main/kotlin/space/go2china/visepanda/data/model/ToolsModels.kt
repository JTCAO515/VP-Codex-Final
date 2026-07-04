package space.go2china.visepanda.data.model

/**
 * v0.3.13: mirrors the web app's `lib/tools/types.ts` `ToolCategory` shape
 * field-for-field (see DESIGN.md ADR-117) so the checklist content
 * and the 3 interactive widgets are the same on both platforms.
 */
data class ToolCategory(
    val id: String,
    val name: String,
    val summary: String,
    val tips: List<String>,
    val sections: List<ToolSection>,
    val offlineTips: List<String>,
    val apiPriority: String,
    val interactive: ToolInteractiveDescriptor? = null,
)

data class ToolSection(
    val title: String,
    val items: List<String>,
)

sealed class ToolInteractiveDescriptor {
    data class CurrencyConverter(
        val baseCurrency: String,
        val defaultTarget: String,
        val commonAmounts: List<Int>,
        val rates: Map<String, Double>,
        // v0.3.13: true once ToolsRepository has successfully merged in a live
        // /api/exchange-rate response.
        val ratesAreLive: Boolean = false,
    ) : ToolInteractiveDescriptor()

    data class VisaChecker(
        val nationalities: List<VisaNationality>,
    ) : ToolInteractiveDescriptor()

    data class PaymentWizard(
        val wallets: List<PaymentWallet>,
        val cardBrands: List<PaymentCardBrand>,
    ) : ToolInteractiveDescriptor()
}

data class VisaNationality(
    val id: String,
    val label: String,
    val visaFreeDays: Int? = null,
    val transitHours: Int? = null,
    val note: String,
)

data class PaymentWallet(
    val id: String,
    val label: String,
    val appName: String,
)

data class PaymentCardBrand(
    val id: String,
    val label: String,
    val note: String,
)
