import Foundation
import SwiftUI

@MainActor
final class TripStore: ObservableObject {
    @Published var selectedTab: RootTab = .chat
    @Published var trip: TripState
    @Published var messages: [ChatMessage]
    @Published var suggestions: [String]
    @Published var isSending = false

    private let api = VisePandaAPIClient()
    private let persistenceKey = "space.go2china.visepanda.ios.localState.v3"

    init() {
        if let persisted = Self.loadPersistedState(key: persistenceKey) {
            trip = persisted.trip
            messages = persisted.messages
            suggestions = persisted.suggestions
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
        selectedTab = .chat
        send("Add \(placeName) to my Shanghai plan if it fits the current pace.")
    }

    func resetLocalDraft() {
        trip = StarterTripData.initialTrip
        messages = StarterTripData.seedMessages
        suggestions = StarterTripData.suggestions
        persist()
    }

    private func resolve(_ text: String) async {
        do {
            let response = try await api.sendChat(
                message: text,
                trip: trip,
                messages: messages,
                preferenceProfile: StarterTripData.preferenceProfile
            )
            let updatedTrip = CanvasPatchApplier.apply(current: trip, patch: response.patch)
            let assistant = ChatMessage(
                id: UUID().uuidString,
                role: .assistant,
                content: response.patch.assistantMessage,
                response: response.patch.assistantResponse,
                createdAt: ISO8601DateFormatter().string(from: Date())
            )
            trip = updatedTrip
            messages.append(assistant)
            suggestions = response.suggestions?.isEmpty == false ? response.suggestions! : StarterTripData.suggestions
        } catch {
            messages.append(serviceUnavailableMessage(error))
            suggestions = StarterTripData.suggestions
        }

        isSending = false
        persist()
    }

    private func serviceUnavailableMessage(_ error: Error) -> ChatMessage {
        ChatMessage(
            id: UUID().uuidString,
            role: .assistant,
            content: "VisePanda could not reach the live Butler service. Your saved trip was not changed.",
            response: AssistantResponse(
                headline: "Live Butler unavailable",
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
        let state = PersistedTripState(trip: trip, messages: messages, suggestions: suggestions)
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
}
