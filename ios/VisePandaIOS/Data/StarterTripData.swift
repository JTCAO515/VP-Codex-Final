import Foundation

enum StarterTripData {
    static let initialTrip = TripState(
        summary: TripSummary(
            title: "New China Trip",
            durationDays: 0,
            pace: .balanced,
            travelerStyle: "Not set yet",
            destinations: [],
            confidence: .draft
        ),
        days: [],
        alerts: [],
        lastUpdatedReason: "Starter draft"
    )

    static let seedMessages: [ChatMessage] = [
        ChatMessage(
            id: UUID().uuidString,
            role: .assistant,
            content: "VisePanda helps you plan and handle a China trip. Ask me what you need next.",
            response: AssistantResponse(
                headline: "What VisePanda can do",
                body: "I can help plan and adjust your China itinerary, find places that fit your pace, prepare transport and entry details, translate travel phrases, and keep useful tools available offline.",
                highlights: [
                    "Build or adjust day plans around weather, pace, and interests",
                    "Find food, attractions, hotels, transport notes, and Travel Talk Cards",
                    "Help with translation, payment setup, eSIM, entry, emergency, and offline checklists"
                ],
                watchOut: nil,
                nextStep: "Ask me anything about your China trip.",
                toolCards: nil
            ),
            createdAt: ISO8601DateFormatter().string(from: Date())
        )
    ]

    static let suggestions = [
        "Plan my first day in China",
        "Find vegetarian food",
        "Make a Travel Talk Card",
        "Translate a travel phrase"
    ]

    static let preferenceProfile = UserPreferenceProfile(
        pace: "balanced",
        budget: "mid",
        party: nil,
        dietaryRestrictions: ["vegetarian"],
        cuisinePreferences: [],
        interests: ["tea houses"],
        profileConfidence: "low"
    )
}
