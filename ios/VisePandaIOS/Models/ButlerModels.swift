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

struct ButlerExploreRef: Codable, Identifiable, Equatable, Hashable {
    var amapPoiId: String
    var name: String
    var cityId: String
    var category: String
    var subcategory: String?
    var rating: Double?
    var pricePerPerson: String?
    var editorial: Bool?

    var id: String { amapPoiId }

    enum CodingKeys: String, CodingKey {
        case amapPoiId
        case name
        case cityId
        case category
        case subcategory
        case rating
        case pricePerPerson
        case editorial
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        amapPoiId = try container.decode(String.self, forKey: .amapPoiId)
        name = try container.decode(String.self, forKey: .name)
        cityId = try container.decode(String.self, forKey: .cityId)
        category = try container.decode(String.self, forKey: .category)
        subcategory = try container.decodeIfPresent(String.self, forKey: .subcategory)
        rating = try container.decodeFlexibleDouble(forKey: .rating)
        pricePerPerson = try container.decodeFlexibleString(forKey: .pricePerPerson)
        editorial = try container.decodeFlexibleBool(forKey: .editorial)
    }

    init(
        amapPoiId: String,
        name: String,
        cityId: String,
        category: String,
        subcategory: String? = nil,
        rating: Double? = nil,
        pricePerPerson: String? = nil,
        editorial: Bool? = nil
    ) {
        self.amapPoiId = amapPoiId
        self.name = name
        self.cityId = cityId
        self.category = category
        self.subcategory = subcategory
        self.rating = rating
        self.pricePerPerson = pricePerPerson
        self.editorial = editorial
    }
}

private extension KeyedDecodingContainer {
    func decodeFlexibleString(forKey key: Key) throws -> String? {
        if let value = try? decodeIfPresent(String.self, forKey: key) {
            return value.isEmpty ? nil : value
        }
        if let value = try? decodeIfPresent(Double.self, forKey: key) {
            return String(format: "%.0f", value)
        }
        return nil
    }

    func decodeFlexibleDouble(forKey key: Key) throws -> Double? {
        if let value = try? decodeIfPresent(Double.self, forKey: key) {
            return value
        }
        if let value = try? decodeIfPresent(String.self, forKey: key) {
            return Double(value)
        }
        return nil
    }

    func decodeFlexibleBool(forKey key: Key) throws -> Bool? {
        if let value = try? decodeIfPresent(Bool.self, forKey: key) {
            return value
        }
        if let value = try? decodeIfPresent(String.self, forKey: key) {
            return Bool(value)
        }
        return nil
    }
}

struct AssistantResponse: Codable, Equatable {
    var headline: String
    var body: String
    var highlights: [String]
    var watchOut: String?
    var nextStep: String
    var toolCards: [InlineToolCard]?
    var exploreRefs: [ButlerExploreRef]? = nil
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
    var affectedDays: [Int]? = nil
    var generationStage: String? = nil
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
    var affectedDays: [Int]? = nil
    /** Present only on assistant messages that actually changed the trip — see ChangeDigest.swift. */
    var changeDigest: [ChangeDigestEntry]? = nil
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
    var completeSkeletonFor: TripState? = nil
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

struct UserMemoryEntry: Codable, Identifiable, Equatable {
    var key: String
    var value: String
    var confidence: Double
    var evidence: [String]
    var source: String
    var updatedAt: String

    var id: String { "\(key)|\(value)|\(updatedAt)" }
}

struct UserMemoryProfileResponse: Codable, Equatable {
    var ok: Bool
    var entries: [UserMemoryEntry]
}

struct UserMemoryDeleteResponse: Codable, Equatable {
    var ok: Bool
    var removed: Bool
}
