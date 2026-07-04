import Foundation

enum CanvasPatchIntent: String, Codable {
    case createTrip = "create_trip"
    case adjustTrip = "adjust_trip"
    case addAlerts = "add_alerts"
}

enum InlineToolCardTone: String, Codable {
    case info
    case warning
    case success
}

struct InlineToolCard: Codable, Identifiable, Equatable {
    var id: String
    var categoryId: String
    var title: String
    var summary: String
    var items: [String]
    var nextAction: String
    var href: String?
    var tone: InlineToolCardTone?
    var sourceLabel: String?
}

struct AssistantResponse: Codable, Equatable {
    var headline: String
    var body: String
    var highlights: [String]
    var watchOut: String?
    var nextStep: String
    var toolCards: [InlineToolCard]?
}

struct TripSummaryPatch: Codable, Equatable {
    var title: String?
    var durationDays: Int?
    var pace: Pace?
    var travelerStyle: String?
    var destinations: [String]?
    var confidence: TripConfidence?
}

struct CanvasPatch: Codable, Equatable {
    var intent: CanvasPatchIntent
    var assistantMessage: String
    var assistantResponse: AssistantResponse?
    var tripSummary: TripSummaryPatch?
    var days: [TripDay]?
    var butlerAlerts: [ButlerAlert]?
    var reason: String
}

enum ChatRole: String, Codable {
    case user
    case assistant
}

struct ChatMessage: Codable, Identifiable, Equatable {
    var id: String
    var role: ChatRole
    var content: String
    var response: AssistantResponse?
    var createdAt: String?
}

struct UserPreferenceProfile: Codable, Equatable {
    var pace: String?
    var budget: String?
    var party: String?
    var dietaryRestrictions: [String]
    var cuisinePreferences: [String]
    var interests: [String]
    var profileConfidence: String
}

struct ToolContext: Codable, Equatable {}

struct ButlerChatRequest: Codable {
    var message: String
    var trip: TripState
    var messages: [ChatMessage]
    var preferenceProfile: UserPreferenceProfile?
}

struct ButlerChatResponse: Codable {
    var ok: Bool?
    var fallbackReason: String?
    var mode: String?
    var modelLabel: String?
    var intent: String?
    var strategy: String?
    var providersTried: [String]?
    var patch: CanvasPatch
    var suggestions: [String]?
    var toolContext: ToolContext?
}

struct ButlerTurnResult {
    var assistantMessage: ChatMessage
    var trip: TripState
    var suggestions: [String]
    var modelLabel: String
    var offlineFallback: Bool
}
