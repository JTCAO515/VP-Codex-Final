import Foundation

enum Pace: String, Codable {
    case light = "Light"
    case balanced = "Balanced"
    case relaxed = "Relaxed"
    case packed = "Packed"
}

enum AlertPriority: String, Codable {
    case high
    case medium
    case low
}

enum AlertType: String, Codable {
    case visa
    case payment
    case booking
    case transport
    case weather
    case language
    case risk
    case emergency
}

enum BlockTime: String, Codable {
    case morning = "Morning"
    case afternoon = "Afternoon"
    case evening = "Evening"
    case flexible = "Flexible"
}

enum BookingCandidateKind: String, Codable {
    case hotel
    case ticket
    case transport
    case restaurant
}

enum BookingCandidateStatus: String, Codable {
    case infoOnly = "info-only"
    case planned
}

struct Coordinates: Codable, Equatable {
    var lat: Double
    var lng: Double
}

struct BookingCandidate: Codable, Identifiable, Equatable {
    var id: String
    var kind: BookingCandidateKind
    var label: String
    var provider: String
    var status: BookingCandidateStatus
    var note: String
    var url: String?
    var priceHint: String?
}

struct TripBlock: Codable, Identifiable, Equatable {
    var id: String { "\(time.rawValue)-\(title)" }
    var time: BlockTime
    var title: String
    var description: String
    var highlights: [String]?
    var photoUrl: String?
    var address: String?
    var chineseAddress: String?
    var phone: String?
    var openingHours: String?
    var mapUrl: String?
    var bookingUrl: String?
    var bookingCandidates: [BookingCandidate]?
    var sourceLabel: String?
    var coordinates: Coordinates?
}

enum DayStatus: String, Codable {
    case new
    case revised
    case needsConfirmation = "needs-confirmation"
}

struct TripDay: Codable, Identifiable, Equatable {
    var id: Int { day }
    var day: Int
    var city: String
    var pace: Pace
    var blocks: [TripBlock]
    var food: [String]
    var stay: String
    var transport: String
    var note: String
    var status: DayStatus?
}

struct ButlerAlert: Codable, Identifiable, Equatable {
    var id: String { "\(type.rawValue)-\(title)" }
    var type: AlertType
    var priority: AlertPriority
    var title: String
    var body: String
    var action: String
    var done: Bool?
}

enum TripConfidence: String, Codable {
    case draft = "Draft"
    case refined = "Refined"
    case readyToSave = "Ready to save"
}

struct TripSummary: Codable, Equatable {
    var title: String
    var durationDays: Int
    var pace: Pace
    var travelerStyle: String
    var destinations: [String]
    var confidence: TripConfidence
}

struct TripState: Codable, Equatable {
    var summary: TripSummary
    var days: [TripDay]
    var alerts: [ButlerAlert]
    var lastUpdatedReason: String
}
