import SwiftUI

struct ToolsView: View {
    private let tools = ToolEntry.seed
    private let columns = [
        GridItem(.flexible(), spacing: 14),
        GridItem(.flexible(), spacing: 14)
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    header

                    LazyVGrid(columns: columns, spacing: 14) {
                        ForEach(tools) { tool in
                            NavigationLink {
                                if tool.id == "translate" {
                                    TranslateView()
                                } else {
                                    ToolDetailView(tool: tool)
                                }
                            } label: {
                                ToolTile(tool: tool)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 14)
                .padding(.bottom, 20)
            }
            .background(VPColor.paper)
            .navigationBarHidden(true)
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text("Tools")
                .font(VPFont.display(26))
                .foregroundStyle(VPColor.ink)
            Text("Fast utilities for the trip")
                .font(VPFont.body(13, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

}

private struct ToolEntry: Identifiable {
    var id: String
    var title: String
    var subtitle: String
    var icon: String
    var badge: String
    var accent: Color
    var highlighted: Bool = false
    var tips: [String]
    var sections: [ToolSection]
    var offlineTips: [String]

    static let sharedOfflineTips = [
        "Screenshot this page before you leave Wi-Fi.",
        "Keep hotel addresses and emergency contacts saved in Chinese characters."
    ]

    static let seed = [
        ToolEntry(
            id: "translate",
            title: "Translate",
            subtitle: "Text phrase helper",
            icon: "translate",
            badge: "Live API",
            accent: VPColor.cinnabar,
            highlighted: true,
            tips: ["Use short sentences for better travel translation.", "Show the Chinese result first when talking to staff or local people."],
            sections: [ToolSection(title: "Good phrases", items: ["Please take me to this address.", "I am vegetarian.", "Can I pay by Alipay?"])],
            offlineTips: sharedOfflineTips + ["Save key phrases before entering stations, restaurants, taxis, or hotels."]
        ),
        ToolEntry(
            id: "emergency",
            title: "Emergency",
            subtitle: "Contacts and documents",
            icon: "sos.circle",
            badge: "Emergency",
            accent: VPColor.cinnabar,
            highlighted: true,
            tips: ["Police: 110", "Ambulance: 120", "Fire: 119"],
            sections: [
                ToolSection(title: "Contacts", items: ["Save embassy contact.", "Save insurance hotline.", "Share itinerary with a trusted contact."]),
                ToolSection(title: "Documents", items: ["Carry passport and visa photos separately.", "Keep medication and allergy notes in English and Chinese."])
            ],
            offlineTips: sharedOfflineTips + ["Pin hotel address and nearest hospital in your maps app."]
        ),
        ToolEntry(
            id: "payment-setup",
            title: "Payment",
            subtitle: "Wallet checklist",
            icon: "creditcard",
            badge: "Pre-trip",
            accent: Color(hex: 0x3B82F6),
            tips: [
                "Most local merchants expect Alipay or WeChat Pay.",
                "Carry a small amount of RMB as backup.",
                "Tell your home bank you are traveling."
            ],
            sections: [
                ToolSection(title: "Before departure", items: ["Install Alipay or WeChat Pay.", "Link an international card.", "Tell your bank about travel."]),
                ToolSection(title: "Backup plan", items: ["Keep one physical card separate from your phone.", "Carry emergency RMB cash."])
            ],
            offlineTips: sharedOfflineTips + ["Write down your card issuer support number outside the payment app."]
        ),
        ToolEntry(
            id: "metro",
            title: "Transport",
            subtitle: "Metro and taxi basics",
            icon: "bus",
            badge: "Transit",
            accent: Color(hex: 0x0EA5E9),
            tips: ["Save destination station names in English and Chinese.", "Check last-train time before late dinners."],
            sections: [
                ToolSection(title: "Metro", items: ["Look for transit QR inside Alipay or WeChat Pay.", "Station machines usually sell single-ride tickets."]),
                ToolSection(title: "Taxi", items: ["Show Chinese address first.", "Confirm destination before the ride starts."])
            ],
            offlineTips: sharedOfflineTips + ["Download metro maps for planned cities before the trip."]
        ),
        ToolEntry(
            id: "currency",
            title: "Currency",
            subtitle: "RMB converter",
            icon: "yensign.circle",
            badge: "Live when configured",
            accent: VPColor.gold,
            tips: ["Use RMB cash as backup even if mobile payment works.", "Check current rates before large exchanges."],
            sections: [
                ToolSection(title: "Cash basics", items: ["Use bank ATMs where possible.", "Check home-bank foreign withdrawal fees."])
            ],
            offlineTips: sharedOfflineTips + ["Save a rough mental conversion for common RMB amounts."]
        ),
        ToolEntry(
            id: "network",
            title: "Network",
            subtitle: "Connectivity",
            icon: "wifi",
            badge: "Offline guide",
            accent: Color(hex: 0x10B981),
            tips: ["Buy a China-compatible eSIM before arrival.", "Test eSIM and VPN setup before departure."],
            sections: [
                ToolSection(title: "Connectivity", items: ["Keep QR activation code accessible offline.", "Confirm mainland China support."]),
                ToolSection(title: "App access", items: ["Check which home-country apps you need.", "Save VPN backup codes offline."])
            ],
            offlineTips: sharedOfflineTips + ["Save eSIM support instructions offline."]
        ),
        ToolEntry(
            id: "visa-and-entry",
            title: "Entry Checklist",
            subtitle: "Visa and arrival",
            icon: "checkmark.shield",
            badge: "Required",
            accent: Color(hex: 0x6366F1),
            tips: [
                "Confirm passport has at least 6 months validity and 2 blank pages.",
                "Have onward travel and first hotel proof ready.",
                "Always confirm current rules on an official embassy or consulate site."
            ],
            sections: [
                ToolSection(title: "Before departure", items: ["Save passport photo page.", "Save visa page if needed.", "Save first hotel and onward ticket."]),
                ToolSection(title: "At arrival", items: ["Keep hotel address in English and Chinese.", "Use the same route details you gave the airline."])
            ],
            offlineTips: sharedOfflineTips + ["Save embassy or consulate contact details for each city."]
        ),
        ToolEntry(
            id: "offline-pack",
            title: "Offline Pack",
            subtitle: "Local survival kit",
            icon: "icloud.and.arrow.down",
            badge: "Local cache",
            accent: VPColor.sage,
            tips: ["Current MVP stores trip and messages locally.", "Full city packs will move to SwiftData later."],
            sections: [
                ToolSection(title: "Saved locally now", items: ["Trip canvas", "Chat history", "Tool checklists", "Fallback translation phrases"]),
                ToolSection(title: "Next", items: ["City JSON packs", "Map pins", "OCR phrase history"])
            ],
            offlineTips: sharedOfflineTips + ["Open this page before travel to confirm local data is available."]
        )
    ]
}

private struct ToolSection: Hashable {
    var title: String
    var items: [String]
}

private struct ToolTile: View {
    let tool: ToolEntry

    var body: some View {
        VPCard {
            VStack(alignment: .leading, spacing: 14) {
                Image(systemName: tool.icon)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(tool.highlighted ? VPColor.paperSoft : tool.accent)
                    .frame(width: 48, height: 48)
                    .background(tool.highlighted ? tool.accent : tool.accent.opacity(0.12))
                    .clipShape(Circle())

                Text(tool.badge)
                    .font(VPFont.body(11, weight: .bold))
                    .foregroundStyle(tool.accent)

                VStack(alignment: .leading, spacing: 3) {
                    Text(tool.title)
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                    Text(tool.subtitle)
                        .font(VPFont.body(12, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                }
            }
            .frame(maxWidth: .infinity, minHeight: 130, alignment: .leading)
        }
    }
}

private struct ToolDetailView: View {
    let tool: ToolEntry

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                detailHeader

                if tool.id == "currency" {
                    CurrencyToolPanel()
                }

                checklistSection(title: "Tips", items: tool.tips, icon: "sparkle")

                ForEach(tool.sections, id: \.title) { section in
                    checklistSection(title: section.title, items: section.items, icon: "checkmark.circle")
                }

                checklistSection(title: "Offline", items: tool.offlineTips, icon: "arrow.down.circle")
            }
            .padding(20)
        }
        .background(VPColor.paper)
        .navigationTitle(tool.title)
        .navigationBarTitleDisplayMode(.inline)
    }

    private var detailHeader: some View {
        VPCard {
            HStack(spacing: 14) {
                Image(systemName: tool.icon)
                    .font(.system(size: 22, weight: .bold))
                    .foregroundStyle(VPColor.paperSoft)
                    .frame(width: 54, height: 54)
                    .background(tool.accent)
                    .clipShape(Circle())

                VStack(alignment: .leading, spacing: 4) {
                    Text(tool.title)
                        .font(VPFont.display(24))
                        .foregroundStyle(VPColor.ink)
                    Text(tool.subtitle)
                        .font(VPFont.body(13, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                }

                Spacer()
            }
        }
    }

    private func checklistSection(title: String, items: [String], icon: String) -> some View {
        VPCard {
            VStack(alignment: .leading, spacing: 12) {
                Text(title.uppercased())
                    .font(VPFont.body(12, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft)

                ForEach(items, id: \.self) { item in
                    Label(item, systemImage: icon)
                        .font(VPFont.body(14, weight: .semibold))
                        .foregroundStyle(VPColor.inkMuted)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
    }
}

private struct CurrencyToolPanel: View {
    @State private var amount = 100.0
    @State private var target = "USD"
    @State private var rates: [String: Double] = [:]
    @State private var status = "Live rates needed"
    @State private var isLoading = false

    private let api = VisePandaAPIClient()
    private let targets = ["USD", "EUR", "GBP", "JPY", "KRW"]

    var body: some View {
        VPCard {
            VStack(alignment: .leading, spacing: 12) {
                Text("CURRENCY")
                    .font(VPFont.body(12, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft)

                HStack {
                    Text("¥")
                        .font(VPFont.display(24))
                        .foregroundStyle(VPColor.ink)
                    TextField("Amount", value: $amount, format: .number)
                        .font(VPFont.body(18, weight: .bold))
                        .keyboardType(.decimalPad)
                }
                .padding(12)
                .background(VPColor.paper)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))

                Picker("Target", selection: $target) {
                    ForEach(targets, id: \.self) { code in
                        Text(code).tag(code)
                    }
                }
                .pickerStyle(.segmented)

                Text(convertedText)
                    .font(VPFont.display(24))
                    .foregroundStyle(VPColor.ink)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(12)
                    .background(VPColor.paperWarm)
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))

                HStack {
                    Text(status)
                        .font(VPFont.body(12, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                    Spacer()
                    Button {
                        refresh()
                    } label: {
                        if isLoading {
                            ProgressView()
                        } else {
                            Image(systemName: "arrow.clockwise")
                        }
                    }
                    .tint(VPColor.cinnabar)
                }
            }
        }
        .task {
            refresh()
        }
    }

    private var convertedText: String {
        guard let rate = rates[target] else {
            return "Live rate unavailable"
        }
        let value = amount * rate
        return "\(target) \(value.formatted(.number.precision(.fractionLength(2))))"
    }

    private func refresh() {
        guard !isLoading else { return }
        isLoading = true
        status = "Refreshing live rates..."

        Task {
            do {
                let response = try await api.fetchExchangeRates()
                if response.ok, let liveRates = response.rates, !liveRates.isEmpty {
                    rates.merge(liveRates) { _, live in live }
                    status = "Rates updated"
                } else {
                    status = "Live rates unavailable. Try again online."
                }
            } catch {
                status = "Live rates unavailable. Try again online."
            }
            isLoading = false
        }
    }
}

struct CommunicationPhrase: Identifiable {
    var id: String { title }
    var title: String
    var chinese: String
    var english: String
    var note: String
    var icon: String
}

struct CommunicationCardDetail: View {
    private let phrases = [
        CommunicationPhrase(
            title: "Start politely",
            chinese: "您好，我不会说中文。可以请您看一下这句话吗？",
            english: "Hello, I don't speak Chinese. Could you please read this?",
            note: "Use this before showing any other card.",
            icon: "hand.wave"
        ),
        CommunicationPhrase(
            title: "Ask for directions",
            chinese: "请问这个地方怎么走？可以帮我指一下方向吗？",
            english: "How do I get to this place? Could you point me in the right direction?",
            note: "Show with your destination name or map pin.",
            icon: "map"
        ),
        CommunicationPhrase(
            title: "Order food",
            chinese: "我是素食者。不吃肉和海鲜。请问有什么推荐？",
            english: "I am vegetarian. I don't eat meat or seafood. What do you recommend?",
            note: "Adjust later for allergies or dietary rules.",
            icon: "fork.knife"
        ),
        CommunicationPhrase(
            title: "Pay",
            chinese: "我可以用支付宝、微信支付或银行卡付款吗？",
            english: "Can I pay with Alipay, WeChat Pay, or a bank card?",
            note: "Useful at restaurants, shops, stations, and hotels.",
            icon: "creditcard"
        ),
        CommunicationPhrase(
            title: "Transport",
            chinese: "请帮我去这个地址。谢谢。",
            english: "Please help me get to this address. Thank you.",
            note: "Use for taxi, hotel front desk, or station staff.",
            icon: "car"
        ),
        CommunicationPhrase(
            title: "Need help",
            chinese: "我需要帮助。可以帮我联系酒店或工作人员吗？",
            english: "I need help. Could you help me contact my hotel or staff?",
            note: "Use when you feel stuck or unsafe.",
            icon: "cross.case"
        )
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                VPCard {
                    VStack(alignment: .leading, spacing: 12) {
                        HStack(spacing: 12) {
                            Image(systemName: "text.bubble.fill")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundStyle(VPColor.paperSoft)
                                .frame(width: 52, height: 52)
                                .background(VPColor.cinnabar)
                                .clipShape(Circle())

                            VStack(alignment: .leading, spacing: 4) {
                                Text("Travel Talk Card")
                                    .font(VPFont.display(30))
                                    .foregroundStyle(VPColor.ink)
                                Text("Show simple phrases when speaking with local people")
                                    .font(VPFont.body(13, weight: .semibold))
                                    .foregroundStyle(VPColor.inkSoft)
                            }
                        }

                        Text("把手机递给对方看，适合问路、点菜、付款、交通、求助等简单交流。")
                            .font(VPFont.body(15, weight: .semibold))
                            .foregroundStyle(VPColor.inkMuted)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }

                ForEach(phrases) { phrase in
                    CommunicationPhraseCard(phrase: phrase)
                }

                VPCard {
                    VStack(alignment: .leading, spacing: 10) {
                        Label("先展示中文，再用英文确认含义", systemImage: "1.circle")
                        Label("涉及地址时，同时打开地图或酒店地址", systemImage: "2.circle")
                        Label("紧急情况优先找酒店、工作人员或警方协助", systemImage: "3.circle")
                    }
                    .font(VPFont.body(15, weight: .semibold))
                    .foregroundStyle(VPColor.inkMuted)
                }
            }
            .padding(20)
        }
        .background(VPColor.paper)
        .navigationTitle("Travel Talk Card")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct CommunicationPhraseCard: View {
    let phrase: CommunicationPhrase

    var body: some View {
        VPCard {
            VStack(alignment: .leading, spacing: 12) {
                HStack(spacing: 10) {
                    Image(systemName: phrase.icon)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(VPColor.cinnabar)
                        .frame(width: 34, height: 34)
                        .background(VPColor.cinnabar.opacity(0.1))
                        .clipShape(Circle())

                    Text(phrase.title)
                        .font(VPFont.body(13, weight: .bold))
                        .foregroundStyle(VPColor.inkSoft)

                    Spacer()
                }

                Text(phrase.chinese)
                    .font(VPFont.display(25))
                    .foregroundStyle(VPColor.ink)
                    .fixedSize(horizontal: false, vertical: true)

                Text(phrase.english)
                    .font(VPFont.body(15, weight: .semibold))
                    .foregroundStyle(VPColor.inkMuted)
                    .fixedSize(horizontal: false, vertical: true)

                Text(phrase.note)
                    .font(VPFont.body(12, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
                    .padding(10)
                    .background(VPColor.paperWarm)
                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
            }
        }
    }
}
