import Foundation
import SwiftUI

@MainActor
final class TripStore: ObservableObject {
    @Published var selectedTab: RootTab = .chat
    @Published var trip: TripState
    @Published var messages: [ChatMessage]
    @Published var suggestions: [String]
    @Published var isSending = false
    @Published var pendingExploreRef: ButlerExploreRef?
    @Published var pendingChatDraft: String?
    @Published var pendingToolId: String?
    @Published var pendingTripDayNumber: Int?
    @Published var recentlyUpdatedDays: Set<Int> = []
    @Published var pendingDetailDays: Set<Int> = []
    @Published var failedDetailDays: Set<Int> = []
    @Published var hidesBottomBar = false
    @Published private(set) var blockNotes: [String: String] = [:]

    private let api = VisePandaAPIClient()
    private var isCompletingSkeleton = false
    private let persistenceKey = "space.go2china.visepanda.ios.localState.v3"

    init() {
        if let persisted = Self.loadPersistedState(key: persistenceKey) {
            trip = persisted.trip
            messages = persisted.messages
            suggestions = persisted.suggestions
            blockNotes = persisted.blockNotes ?? [:]
        } else {
            trip = StarterTripData.initialTrip
            messages = StarterTripData.seedMessages
            suggestions = StarterTripData.suggestions
        }
    }

    func send(_ rawText: String) {
        let text = rawText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, !isSending else { return }

        let userMessage = ChatMessage(
            id: UUID().uuidString,
            role: .user,
            content: text,
            response: nil,
            createdAt: ISO8601DateFormatter().string(from: Date())
        )
        messages.append(userMessage)
        isSending = true
        persist()

        Task {
            await resolve(text)
        }
    }

    func addPlaceToPlan(_ placeName: String) {
        send("Add \(placeName) to my Shanghai plan if it fits the current pace.")
    }

    func openExplore(ref: ButlerExploreRef) {
        pendingExploreRef = ref
        selectedTab = .explore
    }

    func openTripDay(_ dayNumber: Int) {
        pendingTripDayNumber = dayNumber
        selectedTab = .trips
    }

    func prefillChat(_ text: String) {
        pendingChatDraft = text
        selectedTab = .chat
    }

    func openTool(_ id: String) {
        pendingToolId = normalizedToolId(id)
        selectedTab = .tools
    }

    func consumePendingToolId() -> String? {
        defer { pendingToolId = nil }
        return pendingToolId
    }

    func consumePendingTripDayNumber() -> Int? {
        defer { pendingTripDayNumber = nil }
        return pendingTripDayNumber
    }

    func setBottomBarHidden(_ hidden: Bool) {
        hidesBottomBar = hidden
    }

    private func normalizedToolId(_ id: String) -> String {
        switch id {
        case "language":
            return "translate"
        case "esim-vpn":
            return "network"
        default:
            return id
        }
    }

    func updateBlockDescription(dayNumber: Int, blockId: String, description: String) {
        guard let dayIndex = trip.days.firstIndex(where: { $0.day == dayNumber }),
              let blockIndex = trip.days[dayIndex].blocks.firstIndex(where: { $0.id == blockId }) else { return }
        trip.days[dayIndex].blocks[blockIndex].description = description
        trip.lastUpdatedReason = "Edited a day block locally."
        persist()
    }

    func moveBlock(dayNumber: Int, from source: Int, to destination: Int) {
        guard let dayIndex = trip.days.firstIndex(where: { $0.day == dayNumber }),
              trip.days[dayIndex].blocks.indices.contains(source),
              trip.days[dayIndex].blocks.indices.contains(destination) else { return }
        trip.days[dayIndex].blocks.swapAt(source, destination)
        trip.lastUpdatedReason = "Reordered a day block locally."
        persist()
    }

    func blockNote(dayNumber: Int, blockId: String) -> String? {
        blockNotes[blockNoteKey(dayNumber: dayNumber, blockId: blockId)]
    }

    func updateBlockNote(dayNumber: Int, blockId: String, note: String) {
        let key = blockNoteKey(dayNumber: dayNumber, blockId: blockId)
        let trimmed = note.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            blockNotes.removeValue(forKey: key)
        } else {
            blockNotes[key] = trimmed
        }
        persist()
    }

    func consumePendingChatDraft() -> String? {
        defer { pendingChatDraft = nil }
        return pendingChatDraft
    }

    func resetLocalDraft() {
        trip = StarterTripData.initialTrip
        messages = StarterTripData.seedMessages
        suggestions = StarterTripData.suggestions
        pendingDetailDays.removeAll()
        failedDetailDays.removeAll()
        blockNotes.removeAll()
        persist()
    }

    func retrySkeletonDetails(for dayNumber: Int) {
        pendingDetailDays.insert(dayNumber)
        failedDetailDays.remove(dayNumber)
        let skeletonTrip = trip
        Task {
            await completeSkeletonDetails(for: skeletonTrip, dayNumbers: [dayNumber])
        }
    }

    private func resolve(_ text: String) async {
        do {
            let preferenceProfile = StarterTripData.preferenceProfile
            let response = try await api.sendChat(
                message: text,
                trip: trip,
                messages: messages,
                preferenceProfile: preferenceProfile
            )
            let updatedTrip = CanvasPatchApplier.apply(current: trip, patch: response.patch)
            let affectedDays = response.patch.affectedDays
            let existingAffectedDays = affectedDays?.filter { day in
                updatedTrip.days.contains { $0.day == day }
            }
            let changeDigest = ChangeDigest.compute(previous: trip, next: updatedTrip)
            let assistant = ChatMessage(
                id: UUID().uuidString,
                role: .assistant,
                content: response.patch.assistantMessage,
                response: response.patch.assistantResponse,
                affectedDays: affectedDays,
                changeDigest: changeDigest.isEmpty ? nil : changeDigest,
                usedPreferenceSummaries: preferenceSummaries(from: preferenceProfile),
                createdAt: ISO8601DateFormatter().string(from: Date())
            )
            trip = updatedTrip
            if response.patch.intent == .createTrip {
                blockNotes.removeAll()
            }
            clearDetailStateForFilledDays(in: updatedTrip)
            recentlyUpdatedDays = Set(existingAffectedDays ?? [])
            messages.append(assistant)
            suggestions = response.suggestions?.isEmpty == false ? response.suggestions! : StarterTripData.suggestions
            if response.patch.generationStage == "skeleton" {
                let skeletonDays = Set(updatedTrip.days.filter(\.blocks.isEmpty).map(\.day))
                pendingDetailDays = skeletonDays
                failedDetailDays.subtract(skeletonDays)
                Task {
                    await completeSkeletonDetails(for: updatedTrip, dayNumbers: skeletonDays)
                }
            }
        } catch {
            messages.append(serviceUnavailableMessage(error))
            suggestions = StarterTripData.suggestions
        }

        isSending = false
        persist()
    }

    private func completeSkeletonDetails(for skeletonTrip: TripState, dayNumbers: Set<Int>) async {
        guard !dayNumbers.isEmpty, !isCompletingSkeleton else { return }
        isCompletingSkeleton = true
        defer { isCompletingSkeleton = false }

        do {
            let response = try await api.sendChat(
                message: "",
                trip: skeletonTrip,
                messages: messages,
                preferenceProfile: StarterTripData.preferenceProfile,
                completeSkeletonFor: skeletonTrip
            )
            let updatedTrip = CanvasPatchApplier.apply(current: trip, patch: response.patch)
            let completedDays = Set((response.patch.days ?? updatedTrip.days).map(\.day))
            trip = updatedTrip
            clearDetailStateForFilledDays(in: updatedTrip)
            pendingDetailDays.subtract(completedDays)
            failedDetailDays.subtract(completedDays)
            recentlyUpdatedDays = completedDays.intersection(Set(updatedTrip.days.map(\.day)))
            suggestions = response.suggestions?.isEmpty == false ? response.suggestions! : suggestions
        } catch {
            pendingDetailDays.subtract(dayNumbers)
            failedDetailDays.formUnion(dayNumbers)
        }

        persist()
    }

    private func clearDetailStateForFilledDays(in trip: TripState) {
        let filledDays = Set(trip.days.filter { !$0.blocks.isEmpty }.map(\.day))
        pendingDetailDays.subtract(filledDays)
        failedDetailDays.subtract(filledDays)
    }

    private func blockNoteKey(dayNumber: Int, blockId: String) -> String {
        "\(dayNumber)-\(blockId)"
    }

    private func preferenceSummaries(from profile: UserPreferenceProfile?) -> [String]? {
        guard let profile else { return nil }
        let summaries = [
            profile.dietaryRestrictions.first.map { "Dietary: \(shortPreferenceValue($0))" },
            profile.interests.first.map { "Interest: \(shortPreferenceValue($0))" },
            profile.pace.map { "Pace: \(shortPreferenceValue($0))" },
            profile.budget.map { "Budget: \(shortPreferenceValue($0))" },
            profile.party.map { "Party: \(shortPreferenceValue($0))" },
            profile.cuisinePreferences.first.map { "Cuisine: \(shortPreferenceValue($0))" }
        ].compactMap { $0 }
        return summaries.isEmpty ? nil : Array(summaries.prefix(3))
    }

    private func shortPreferenceValue(_ value: String) -> String {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.count > 36 ? "\(trimmed.prefix(36))…" : trimmed
    }

    private func serviceUnavailableMessage(_ error: Error) -> ChatMessage {
        ChatMessage(
            id: UUID().uuidString,
            role: .assistant,
            content: "VisePanda could not reach the live Copilot service. Your saved trip was not changed.",
            response: AssistantResponse(
                headline: "Live Copilot unavailable",
                body: "I could not reach /api/chat, so I did not create or edit your itinerary. You can still view saved trip data and use the local Tools and Explore starter lists.",
                highlights: [
                    "No AI or third-party key is stored in this iOS app",
                    "Trip changes only happen after a backend CanvasPatch returns",
                    "Local starter content remains available offline"
                ],
                watchOut: "Network or backend service is unavailable right now.",
                nextStep: "Try again when the network or backend is available.",
                toolCards: nil
            ),
            createdAt: ISO8601DateFormatter().string(from: Date())
        )
    }

    private func persist() {
        let state = PersistedTripState(trip: trip, messages: messages, suggestions: suggestions, blockNotes: blockNotes)
        guard let data = try? JSONEncoder.visePanda.encode(state) else { return }
        UserDefaults.standard.set(data, forKey: persistenceKey)
    }

    private static func loadPersistedState(key: String) -> PersistedTripState? {
        guard let data = UserDefaults.standard.data(forKey: key) else { return nil }
        return try? JSONDecoder.visePanda.decode(PersistedTripState.self, from: data)
    }
}

private struct PersistedTripState: Codable {
    var trip: TripState
    var messages: [ChatMessage]
    var suggestions: [String]
    var blockNotes: [String: String]?
}
