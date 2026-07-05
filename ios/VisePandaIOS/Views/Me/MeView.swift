import SwiftUI

struct MeView: View {
    @EnvironmentObject private var store: TripStore
    @EnvironmentObject private var authStore: AuthStore
    @AppStorage("space.go2china.visepanda.ios.memoryProfileUserId") private var storedUserId = ""
    @State private var entries: [UserMemoryEntry] = []
    @State private var isLoadingProfile = false
    @State private var errorMessage: String?
    @State private var selectedEntry: UserMemoryEntry?
    @State private var deletingEntryId: String?
    @State private var deleteNotice: String?
    @State private var authEmail = ""
    @State private var authPassword = ""
    @State private var showingAuth = false
    @State private var purchaseNotice: String?

    private let api = VisePandaAPIClient()

    init() {}

    #if DEBUG
    init(screenshotScenario: MeScreenshotScenario) {
        let sample = UserMemoryEntry(
            key: "dietary",
            value: "vegetarian",
            confidence: 0.95,
            evidence: ["I am vegetarian"],
            source: "explicit",
            updatedAt: "2026-07-05T10:30:00Z"
        )
        switch screenshotScenario {
        case .profile, .authSignedIn, .authSignedOut, .authOffline:
            _entries = State(initialValue: [
                sample,
                UserMemoryEntry(
                    key: "pace",
                    value: "relaxed",
                    confidence: 0.82,
                    evidence: ["I prefer slow mornings"],
                    source: "inferred",
                    updatedAt: "2026-07-05T10:34:00Z"
                )
            ])
        case .delete:
            _entries = State(initialValue: [sample])
            _selectedEntry = State(initialValue: sample)
        case .offline:
            _errorMessage = State(initialValue: "Could not reach /butler/memory/profile. Check the network and try again.")
        }
    }
    #endif

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text("Me")
                    .font(VPFont.display(26))
                    .foregroundStyle(VPColor.ink)
                    .frame(maxWidth: .infinity, alignment: .leading)

                profileCard
                subscriptionSection
                memoryProfileSection
                section(title: "TRIP HISTORY", rows: trips)
                section(title: "DATA & OFFLINE", rows: dataRows)

                Button(role: .destructive) {
                    store.resetLocalDraft()
                } label: {
                    Text("Reset local iOS draft")
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.cinnabar)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 14)
            .padding(.bottom, 20)
        }
        .background(VPColor.paper)
        .task {
            await authStore.refreshSessionIfNeeded()
            await loadProfileIfNeeded()
        }
        .refreshable {
            await loadProfile(force: true)
        }
        .sheet(item: $selectedEntry) { entry in
            MemoryEntryDetailView(
                entry: entry,
                isDeleting: deletingEntryId == entry.id,
                onDelete: { Task { await delete(entry) } }
            )
        }
        .sheet(isPresented: $showingAuth) {
            AuthSheetView(
                authStore: authStore,
                email: $authEmail,
                password: $authPassword,
                canSubmit: canSubmitAuth
            )
            .presentationDetents([.medium, .large])
        }
        .alert("Subscription placeholder", isPresented: Binding(
            get: { purchaseNotice != nil },
            set: { if !$0 { purchaseNotice = nil } }
        )) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(purchaseNotice ?? "")
        }
    }

    private var profileCard: some View {
        Button {
            showingAuth = true
        } label: {
            HStack(spacing: 14) {
                Text("EM")
                    .font(VPFont.display(16))
                    .foregroundStyle(VPColor.paperSoft)
                    .frame(width: 54, height: 54)
                    .background(VPColor.ink)
                    .clipShape(Circle())

                VStack(alignment: .leading, spacing: 3) {
                    Text(authStore.email ?? "Guest Traveler")
                        .font(VPFont.body(17, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                    Text(authStore.isSignedIn ? "Signed in · Supabase" : "AI profile · \(currentUserId.prefix(8))")
                        .font(VPFont.body(12, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft.opacity(0.7))
            }
            .padding(18)
            .background(VPColor.paperSoft)
            .clipShape(RoundedRectangle(cornerRadius: VPRadius.md, style: .continuous))
            .overlay {
                RoundedRectangle(cornerRadius: VPRadius.md, style: .continuous)
                    .stroke(VPColor.outline.opacity(0.7), lineWidth: 1)
            }
            .shadow(color: VPColor.ink.opacity(0.08), radius: 8, x: 0, y: 4)
        }
        .buttonStyle(.plain)
    }

    private var subscriptionSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("SUBSCRIPTION")
                .font(VPFont.body(12, weight: .bold))
                .foregroundStyle(VPColor.inkSoft)
                .padding(.leading, 4)

            VStack(spacing: 12) {
                SubscriptionPlanCard(
                    title: "Human Service",
                    price: "$9.99 / month",
                    summary: "Priority human travel help for itinerary fixes, booking questions, and urgent trip problems.",
                    actionTitle: "Subscribe"
                ) {
                    purchaseNotice = "StoreKit purchase placeholder. Product id: visepanda.human.monthly"
                }

                SubscriptionPlanCard(
                    title: "Premium Service",
                    price: "$19.99 / month",
                    summary: "Advanced concierge support with deeper planning review and faster response windows.",
                    actionTitle: "Subscribe"
                ) {
                    purchaseNotice = "StoreKit purchase placeholder. Product id: visepanda.premium.monthly"
                }

                Button {
                    purchaseNotice = "Restore purchases placeholder. StoreKit restore will be connected with App Store products."
                } label: {
                    Label("Restore Purchase", systemImage: "arrow.clockwise")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)

                Text("Subscriptions auto-renew until cancelled. Manage or cancel in your Apple ID subscriptions. Payment will be charged to your Apple ID after confirmation. Terms and Privacy Policy links will be connected before release.")
                    .font(VPFont.body(11, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
                    .fixedSize(horizontal: false, vertical: true)

                HStack {
                    Button("Terms of Use") {
                        purchaseNotice = "Terms of Use placeholder."
                    }
                    Spacer()
                    Button("Privacy Policy") {
                        purchaseNotice = "Privacy Policy placeholder."
                    }
                }
                .font(VPFont.body(12, weight: .bold))
                .foregroundStyle(VPColor.cinnabar)
            }
        }
    }

    private var memoryProfileSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("AI PROFILE")
                    .font(VPFont.body(12, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft)
                    .padding(.leading, 4)

                Spacer()

                if isLoadingProfile {
                    ProgressView()
                        .controlSize(.small)
                } else {
                    Button {
                        Task { await loadProfile(force: true) }
                    } label: {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 13, weight: .bold))
                    }
                    .accessibilityLabel("Refresh AI profile")
                    .foregroundStyle(VPColor.cinnabar)
                }
            }

            VPCard(padding: 0) {
                VStack(spacing: 0) {
                    if let errorMessage {
                        MemoryStatusRow(
                            title: "Profile unavailable",
                            value: errorMessage,
                            icon: "wifi.exclamationmark"
                        )
                        .accessibilityIdentifier("memoryProfileOffline")
                    } else if entries.isEmpty {
                        MemoryStatusRow(
                            title: "No AI profile yet",
                            value: "Chat with Butler to build a preference profile.",
                            icon: "sparkles"
                        )
                    } else {
                        ForEach(entries) { entry in
                            Button {
                                selectedEntry = entry
                            } label: {
                                MemoryEntryRow(entry: entry)
                            }
                            .buttonStyle(.plain)
                            .accessibilityIdentifier("memoryProfileEntry")

                            if entry.id != entries.last?.id {
                                Rectangle()
                                    .fill(VPColor.outline.opacity(0.72))
                                    .frame(height: 1)
                            }
                        }
                    }

                    if let deleteNotice {
                        Rectangle()
                            .fill(VPColor.outline.opacity(0.72))
                            .frame(height: 1)
                        MemoryStatusRow(title: "Updated", value: deleteNotice, icon: "checkmark.circle")
                    }
                }
            }
        }
    }

    private var trips: [ProfileRow] {
        [
            ProfileRow(title: store.trip.summary.title, value: "Current"),
            ProfileRow(title: "Saved trip history", value: "Not connected")
        ]
    }

    private var dataRows: [ProfileRow] {
        [
            ProfileRow(title: "Offline storage", value: "Trip + chat cache"),
            ProfileRow(title: "Language", value: "English"),
            ProfileRow(title: "Export my data", value: "Not available")
        ]
    }

    private func section(title: String, rows: [ProfileRow]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(VPFont.body(12, weight: .bold))
                .foregroundStyle(VPColor.inkSoft)
                .padding(.leading, 4)

            VPCard(padding: 0) {
                VStack(spacing: 0) {
                    ForEach(rows) { row in
                        ProfileRowView(row: row)
                        if row.id != rows.last?.id {
                            Rectangle()
                                .fill(VPColor.outline.opacity(0.72))
                                .frame(height: 1)
                        }
                    }
                }
            }
        }
    }

    private var currentUserId: String {
        if let userId = authStore.userId {
            return userId
        }
        if storedUserId.isEmpty {
            let newId = UUID().uuidString.lowercased()
            storedUserId = newId
            return newId
        }
        return storedUserId
    }

    private var canSubmitAuth: Bool {
        authEmail.contains("@") && authPassword.count >= 6
    }

    private func loadProfileIfNeeded() async {
        #if DEBUG
        if MeScreenshotScenario.launchArgument != nil {
            return
        }
        #endif
        await loadProfile(force: false)
    }

    private func loadProfile(force: Bool) async {
        if isLoadingProfile || (!force && (!entries.isEmpty || errorMessage != nil)) {
            return
        }

        isLoadingProfile = true
        defer { isLoadingProfile = false }

        do {
            let response = try await api.fetchMemoryProfile(userId: currentUserId)
            entries = response.ok ? response.entries.sorted { $0.updatedAt > $1.updatedAt } : []
            errorMessage = nil
        } catch {
            errorMessage = "Could not reach /butler/memory/profile. Check the network and try again."
        }
    }

    private func delete(_ entry: UserMemoryEntry) async {
        deletingEntryId = entry.id
        defer { deletingEntryId = nil }

        do {
            let response = try await api.deleteMemoryProfileEntry(userId: currentUserId, key: entry.key, value: entry.value)
            if response.ok, response.removed {
                entries.removeAll { $0.key == entry.key && $0.value == entry.value }
                selectedEntry = nil
                deleteNotice = "Removed \(entry.key): \(entry.value)"
            } else {
                deleteNotice = "No matching memory was removed."
            }
        } catch {
            errorMessage = "Could not delete this profile memory. Try again when the network is available."
        }
    }
}

private struct ProfileRow: Identifiable, Equatable {
    var id: String { title + value }
    var title: String
    var value: String
}

private struct SubscriptionPlanCard: View {
    let title: String
    let price: String
    let summary: String
    let actionTitle: String
    let action: () -> Void

    var body: some View {
        VPCard {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(title)
                            .font(VPFont.body(17, weight: .bold))
                            .foregroundStyle(VPColor.ink)
                        Text(price)
                            .font(VPFont.body(13, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                    }
                    Spacer()
                    Button(actionTitle, action: action)
                        .font(VPFont.body(13, weight: .bold))
                        .buttonStyle(.borderedProminent)
                        .tint(VPColor.cinnabar)
                }

                Text(summary)
                    .font(VPFont.body(13, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}

private struct AuthSheetView: View {
    @ObservedObject var authStore: AuthStore
    @Binding var email: String
    @Binding var password: String
    let canSubmit: Bool

    var body: some View {
        NavigationStack {
            ScrollView {
                VPCard {
                    if authStore.isSignedIn {
                        VStack(alignment: .leading, spacing: 12) {
                            Label(authStore.email ?? "Signed in", systemImage: "person.crop.circle.fill")
                                .font(VPFont.body(15, weight: .bold))
                                .foregroundStyle(VPColor.ink)
                                .accessibilityIdentifier("signedInEmail")

                            Button(role: .destructive) {
                                Task { await authStore.signOut() }
                            } label: {
                                Label(authStore.isLoading ? "Signing out..." : "Sign out", systemImage: "rectangle.portrait.and.arrow.right")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(VPColor.cinnabar)
                            .disabled(authStore.isLoading)
                            .accessibilityIdentifier("signOutButton")
                        }
                    } else {
                        VStack(alignment: .leading, spacing: 12) {
                            TextField("Email", text: $email)
                                .textInputAutocapitalization(.never)
                                .keyboardType(.emailAddress)
                                .autocorrectionDisabled()
                                .textContentType(.username)
                                .authField()
                                .accessibilityIdentifier("authEmail")

                            SecureField("Password", text: $password)
                                .textContentType(.password)
                                .authField()
                                .accessibilityIdentifier("authPassword")

                            HStack(spacing: 10) {
                                Button {
                                    Task { await authStore.signIn(email: email, password: password) }
                                } label: {
                                    Text(authStore.isLoading ? "Working..." : "Sign in")
                                        .frame(maxWidth: .infinity)
                                }
                                .buttonStyle(.borderedProminent)
                                .tint(VPColor.cinnabar)
                                .disabled(authStore.isLoading || !canSubmit)
                                .accessibilityIdentifier("signInButton")

                                Button {
                                    Task { await authStore.signUp(email: email, password: password) }
                                } label: {
                                    Text("Register")
                                        .frame(maxWidth: .infinity)
                                }
                                .buttonStyle(.bordered)
                                .disabled(authStore.isLoading || !canSubmit)
                                .accessibilityIdentifier("registerButton")
                            }
                            .font(VPFont.body(14, weight: .bold))

                            Button {
                                authStore.signInWithGoogle()
                            } label: {
                                Label("Continue with Google", systemImage: "globe")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.bordered)
                            .disabled(authStore.isLoading)

                            if let message = authStore.errorMessage {
                                Label(message, systemImage: "exclamationmark.triangle")
                                    .font(VPFont.body(13, weight: .semibold))
                                    .foregroundStyle(VPColor.cinnabar)
                                    .fixedSize(horizontal: false, vertical: true)
                                    .accessibilityIdentifier("authError")
                            }
                        }
                    }
                }
                .padding(20)
            }
            .background(VPColor.paper)
            .navigationTitle("Account")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

private extension View {
    func authField() -> some View {
        font(VPFont.body(15, weight: .semibold))
            .padding(12)
            .background(VPColor.paper)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

private struct MemoryEntryRow: View {
    let entry: UserMemoryEntry

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "sparkle.magnifyingglass")
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(VPColor.cinnabar)
                .frame(width: 26)

            VStack(alignment: .leading, spacing: 3) {
                Text(entry.key.capitalized)
                    .font(VPFont.body(15, weight: .semibold))
                    .foregroundStyle(VPColor.inkMuted)
                Text(entry.value)
                    .font(VPFont.body(13, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
                    .lineLimit(2)
            }

            Spacer()

            VPStatusPill(title: "\(Int((entry.confidence * 100).rounded()))%", tone: .ready)
            Image(systemName: "chevron.right")
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(VPColor.inkSoft.opacity(0.7))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}

private struct MemoryStatusRow: View {
    let title: String
    let value: String
    let icon: String

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(VPColor.cinnabar)
                .frame(width: 26)

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(VPFont.body(15, weight: .bold))
                    .foregroundStyle(VPColor.ink)
                Text(value)
                    .font(VPFont.body(13, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
                    .fixedSize(horizontal: false, vertical: true)
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}

private struct MemoryEntryDetailView: View {
    @Environment(\.dismiss) private var dismiss
    let entry: UserMemoryEntry
    let isDeleting: Bool
    let onDelete: () -> Void

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VPCard {
                        VStack(alignment: .leading, spacing: 10) {
                            Text(entry.key.capitalized)
                                .font(VPFont.body(12, weight: .bold))
                                .foregroundStyle(VPColor.cinnabar)
                            Text(entry.value)
                                .font(VPFont.display(26, weight: .bold))
                                .foregroundStyle(VPColor.ink)
                            Text("Source: \(entry.source) · Confidence \(Int((entry.confidence * 100).rounded()))%")
                                .font(VPFont.body(13, weight: .semibold))
                                .foregroundStyle(VPColor.inkSoft)
                            Text("Updated \(entry.updatedAt)")
                                .font(VPFont.body(12, weight: .semibold))
                                .foregroundStyle(VPColor.inkSoft)
                        }
                    }

                    VPCard {
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Evidence")
                                .font(VPFont.body(13, weight: .bold))
                                .foregroundStyle(VPColor.inkSoft)

                            ForEach(entry.evidence, id: \.self) { evidence in
                                Text(evidence)
                                    .font(VPFont.body(15, weight: .semibold))
                                    .foregroundStyle(VPColor.inkMuted)
                                    .fixedSize(horizontal: false, vertical: true)
                            }
                        }
                    }

                    Button(role: .destructive) {
                        onDelete()
                    } label: {
                        HStack {
                            if isDeleting {
                                ProgressView()
                            }
                            Text(isDeleting ? "Deleting..." : "Delete this memory")
                                .font(VPFont.body(15, weight: .bold))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(VPColor.cinnabar)
                    .accessibilityIdentifier("deleteMemoryEntry")
                    .disabled(isDeleting)
                }
                .padding(20)
            }
            .background(VPColor.paper)
            .navigationTitle("Profile Memory")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}

#if DEBUG
enum MeScreenshotScenario: String {
    case profile = "me-profile"
    case delete = "me-delete"
    case offline = "me-offline"
    case authSignedOut = "auth-signed-out"
    case authSignedIn = "auth-signed-in"
    case authOffline = "auth-offline"

    static var launchArgument: MeScreenshotScenario? {
        ProcessInfo.processInfo.arguments
            .compactMap(MeScreenshotScenario.init(rawValue:))
            .first
    }
}
#endif

private struct ProfileRowView: View {
    let row: ProfileRow

    var body: some View {
        HStack {
            Text(row.title)
                .font(VPFont.body(15, weight: .semibold))
                .foregroundStyle(VPColor.inkMuted)
            Spacer()
            Text(row.value)
                .font(VPFont.body(13, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
            Image(systemName: "chevron.right")
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(VPColor.inkSoft.opacity(0.7))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}
