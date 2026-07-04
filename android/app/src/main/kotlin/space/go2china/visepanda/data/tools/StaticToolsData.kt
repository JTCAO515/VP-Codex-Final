package space.go2china.visepanda.data.tools

import space.go2china.visepanda.data.model.PaymentCardBrand
import space.go2china.visepanda.data.model.PaymentWallet
import space.go2china.visepanda.data.model.ToolCategory
import space.go2china.visepanda.data.model.ToolInteractiveDescriptor
import space.go2china.visepanda.data.model.ToolSection
import space.go2china.visepanda.data.model.VisaNationality

/**
 * 1:1 content mirror of the web app's `lib/tools/staticProvider.ts` (see
 * DESIGN.md ADR-117) — same 6 categories, same copy, same conservative
 * disclosure language ("confirm with an official embassy," "offline
 * estimate," etc.). Every category shares the same two base offline tips
 * the web version does.
 */
private val sharedOfflineTips = listOf(
    "Screenshot this checklist before you leave Wi-Fi.",
    "Keep hotel addresses and emergency contacts saved in Chinese characters.",
)

object StaticToolsData {

    val categories: List<ToolCategory> = listOf(
        ToolCategory(
            id = "visa-and-entry",
            name = "Visa and entry",
            summary = "Check visa requirements and entry paperwork before you fly.",
            tips = listOf(
                "Confirm your passport has at least 6 months of validity left and 2+ blank pages.",
                "Check whether your nationality qualifies for a visa-free transit policy, and note the exact day limit and the cities it covers.",
                "Have proof of onward or return travel and your first night of accommodation ready for entry checks.",
                "Always confirm current rules on the official embassy or consulate website because visa policy changes often.",
            ),
            sections = listOf(
                ToolSection(
                    title = "Before departure",
                    items = listOf(
                        "Confirm visa-free, visa, or transit eligibility against an official embassy or consulate source.",
                        "Save your passport photo page, visa page, first hotel, and onward or return ticket.",
                    ),
                ),
                ToolSection(
                    title = "At arrival",
                    items = listOf(
                        "Keep your first hotel address and phone number ready in English and Chinese.",
                        "Use the same itinerary details you gave the airline if border staff ask about your route.",
                    ),
                ),
            ),
            offlineTips = sharedOfflineTips + "Save embassy or consulate contact details for each destination city.",
            apiPriority = "Next later: verified visa-rule lookup once an official or trusted provider is selected.",
            interactive = ToolInteractiveDescriptor.VisaChecker(
                nationalities = listOf(
                    VisaNationality(
                        id = "us",
                        label = "United States",
                        transitHours = 240,
                        note = "Tourist trips usually need a visa unless the route qualifies for a transit-without-visa policy.",
                    ),
                    VisaNationality(
                        id = "uk",
                        label = "United Kingdom",
                        transitHours = 240,
                        note = "Tourist trips usually need a visa unless the route qualifies for transit-without-visa.",
                    ),
                    VisaNationality(
                        id = "germany",
                        label = "Germany",
                        visaFreeDays = 30,
                        transitHours = 240,
                        note = "Short ordinary-passport visits may be visa-free under current policy windows, but official confirmation is still required.",
                    ),
                    VisaNationality(
                        id = "singapore",
                        label = "Singapore",
                        visaFreeDays = 30,
                        transitHours = 240,
                        note = "Short ordinary-passport visits may be visa-free; keep return/onward travel proof ready.",
                    ),
                    VisaNationality(
                        id = "japan",
                        label = "Japan",
                        transitHours = 240,
                        note = "Policy can change quickly; confirm the current tourist-visa requirement before booking.",
                    ),
                    VisaNationality(
                        id = "other",
                        label = "Other nationality",
                        transitHours = 240,
                        note = "Use this only as a planning prompt; confirm with the nearest Chinese embassy or consulate.",
                    ),
                ),
            ),
        ),
        ToolCategory(
            id = "payment-setup",
            name = "Payment setup",
            summary = "Set up mobile payment so you are not stuck carrying only cash.",
            tips = listOf(
                "Most local merchants expect Alipay or WeChat Pay; set one up before you arrive if possible.",
                "International cards usually work for the tourist version of Alipay, but daily and transaction limits apply.",
                "Carry a small amount of cash (RMB) as a backup for small vendors that do not take foreign cards.",
                "Notify your home bank of international travel to avoid your card being flagged for fraud.",
            ),
            sections = listOf(
                ToolSection(
                    title = "Before departure",
                    items = listOf(
                        "Install Alipay or WeChat Pay and link an international card before you arrive.",
                        "Tell your bank you are traveling so card verification and top-ups are less likely to fail.",
                    ),
                ),
                ToolSection(
                    title = "Backup plan",
                    items = listOf(
                        "Carry a small amount of RMB for taxis, small vendors, or temporary app issues.",
                        "Keep one physical card separate from your phone.",
                    ),
                ),
            ),
            offlineTips = sharedOfflineTips + "Write down your card issuer support number outside the payment app.",
            apiPriority = "Later: payment setup remains checklist-first; no payment transaction API is planned yet.",
            interactive = ToolInteractiveDescriptor.PaymentWizard(
                wallets = listOf(
                    PaymentWallet(id = "alipay", label = "Alipay", appName = "Alipay"),
                    PaymentWallet(id = "wechat-pay", label = "WeChat Pay", appName = "WeChat"),
                ),
                cardBrands = listOf(
                    PaymentCardBrand(id = "visa", label = "Visa", note = "Usually supported by tourist wallet setup, subject to issuer verification."),
                    PaymentCardBrand(id = "mastercard", label = "Mastercard", note = "Usually supported by tourist wallet setup, subject to issuer verification."),
                    PaymentCardBrand(id = "amex", label = "Amex", note = "Support varies; keep a Visa or Mastercard backup if possible."),
                    PaymentCardBrand(id = "other", label = "Other card", note = "Check issuer compatibility and carry a second payment method."),
                ),
            ),
        ),
        ToolCategory(
            id = "currency",
            name = "Currency",
            summary = "Understand RMB cash and exchange basics.",
            tips = listOf(
                "Exchange a small amount of RMB before you arrive, or use an airport counter for emergency cash.",
                "ATMs at major banks generally accept foreign cards for RMB withdrawals; check your home bank's foreign withdrawal fees first.",
                "Check a current exchange-rate reference before large cash exchanges or ATM withdrawals.",
            ),
            sections = listOf(
                ToolSection(
                    title = "Cash basics",
                    items = listOf(
                        "RMB cash is useful as backup even if mobile payment works most of the time.",
                        "Use bank ATMs when possible and check home-bank foreign withdrawal fees.",
                    ),
                ),
                ToolSection(
                    title = "Rate checks",
                    items = listOf(
                        "Check the current exchange rate with your bank or a trusted currency app before large exchanges.",
                        "Confirm current rates with your bank or a trusted currency app before large exchanges.",
                    ),
                ),
            ),
            offlineTips = sharedOfflineTips + "Save a rough mental conversion for common RMB amounts.",
            apiPriority = "Next later: real-time exchange-rate API is the first Tools data integration candidate.",
            interactive = ToolInteractiveDescriptor.CurrencyConverter(
                baseCurrency = "CNY",
                defaultTarget = "USD",
                commonAmounts = listOf(20, 50, 100, 200, 500),
                rates = emptyMap(),
                ratesAreLive = false,
            ),
        ),
        ToolCategory(
            id = "metro",
            name = "Metro",
            summary = "Get around major cities without a car.",
            tips = listOf(
                "Most metro systems accept the same mobile payment app you set up for daily spending; look for a transit QR code feature.",
                "Single-ride paper tickets are still available at station machines if you prefer not to link payment.",
                "Download the city's official metro app or a maps app with transit directions before you head out.",
            ),
            sections = listOf(
                ToolSection(
                    title = "Tickets and QR codes",
                    items = listOf(
                        "Look for the transit QR feature inside Alipay or WeChat Pay.",
                        "Station machines usually still sell single-ride tickets if app setup fails.",
                    ),
                ),
                ToolSection(
                    title = "Route reading",
                    items = listOf(
                        "Save your destination station in Chinese and English.",
                        "Check the last-train time before late dinners or night views.",
                    ),
                ),
            ),
            offlineTips = sharedOfflineTips + "Download metro maps for your planned cities before the trip.",
            apiPriority = "Later: live transit routing can be added after map or transit provider validation.",
        ),
        ToolCategory(
            id = "esim-vpn",
            name = "eSIM/VPN",
            summary = "Stay connected and reach the apps you rely on at home.",
            tips = listOf(
                "Buy a China-compatible eSIM or local SIM before you need it; activation can take time.",
                "Some foreign apps and sites may not be reachable without a VPN; check what you need access to before travel.",
                "Test your eSIM/VPN setup before departure, not after you land.",
            ),
            sections = listOf(
                ToolSection(
                    title = "Connectivity",
                    items = listOf(
                        "Confirm your eSIM works in mainland China before departure.",
                        "Keep the QR activation code somewhere accessible offline.",
                    ),
                ),
                ToolSection(
                    title = "App access",
                    items = listOf(
                        "Check which home-country apps you must reach during the trip.",
                        "Test your VPN setup before relying on it for travel documents or messaging.",
                    ),
                ),
            ),
            offlineTips = sharedOfflineTips + "Save eSIM support instructions and VPN login backup codes offline.",
            apiPriority = "Later: provider comparison may be content-led first; no live telecom API is planned yet.",
        ),
        ToolCategory(
            id = "emergency",
            name = "Emergency",
            summary = "Know who to call and what to carry in case something goes wrong.",
            tips = listOf(
                "Save local emergency numbers and your embassy contact information offline.",
                "Carry a photo of your passport and visa separate from the originals.",
                "Know the address of your accommodation in Chinese characters in case you need to show it to someone for help.",
            ),
            sections = listOf(
                ToolSection(
                    title = "Contacts",
                    items = listOf(
                        "Save local emergency numbers, embassy contact, insurance hotline, and hotel front desk.",
                        "Share your itinerary and hotel addresses with a trusted contact.",
                    ),
                ),
                ToolSection(
                    title = "Documents",
                    items = listOf(
                        "Carry passport and visa photos separately from the originals.",
                        "Keep medication names and allergy notes in English and Chinese.",
                    ),
                ),
            ),
            offlineTips = sharedOfflineTips + "Pin your hotel address and nearest hospital in your maps app.",
            apiPriority = "Not planned yet: emergency content should stay conservative unless verified local data is available.",
        ),
    )
}
