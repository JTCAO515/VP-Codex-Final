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

enum CompletenessDimension: String {
    case route = "Route"
    case stay = "Stay area"
    case food = "Food"
    case transport = "Transport"
    case payment = "Payment"
    case visa = "Visa"
}

struct CompletenessCheck: Identifiable, Equatable {
    var id: CompletenessDimension
    var complete: Bool
}

struct CompletenessResult: Equatable {
    var checks: [CompletenessCheck]
    var score: Int
}

enum TripCompleteness {
    static func calculateTripCompleteness(_ trip: TripState) -> CompletenessResult {
        func everyDayHas(_ predicate: (TripDay) -> Bool) -> Bool {
            !trip.days.isEmpty && trip.days.allSatisfy(predicate)
        }
        func noOutstandingAlert(_ type: AlertType) -> Bool {
            trip.alerts.filter { $0.type == type }.allSatisfy { $0.done == true }
        }
        let checks = [
            CompletenessCheck(id: .route, complete: !trip.summary.destinations.isEmpty && !trip.days.isEmpty),
            CompletenessCheck(id: .stay, complete: everyDayHas { !$0.stay.isEmpty }),
            CompletenessCheck(id: .food, complete: everyDayHas { !$0.food.isEmpty }),
            CompletenessCheck(id: .transport, complete: everyDayHas { !$0.transport.isEmpty }),
            CompletenessCheck(id: .payment, complete: noOutstandingAlert(.payment)),
            CompletenessCheck(id: .visa, complete: noOutstandingAlert(.visa))
        ]
        let completeCount = checks.filter(\.complete).count
        return CompletenessResult(checks: checks, score: Int(((Double(completeCount) / Double(checks.count)) * 100).rounded()))
    }

    static func calculateDayCompleteness(_ day: TripDay) -> Int {
        let checks = [day.blocks.count >= 3, !day.food.isEmpty, !day.stay.isEmpty, !day.transport.isEmpty]
        return Int(((Double(checks.filter { $0 }.count) / Double(checks.count)) * 100).rounded())
    }
}

enum TimelinePosition: String {
    case now = "NOW"
    case next = "NEXT"
    case later = "LATER"
}

struct TimelineEntry: Identifiable {
    var id: String { "\(day.day)-\(block.id)-\(position.rawValue)" }
    var day: TripDay
    var block: TripBlock
    var position: TimelinePosition
}

enum TripTimeline {
    static func buildTimeline(_ trip: TripState) -> [TimelineEntry] {
        guard let today = trip.days.first else { return [] }
        return today.blocks.enumerated().map { index, block in
            TimelineEntry(day: today, block: block, position: index == 0 ? .now : index == 1 ? .next : .later)
        }
    }
}
