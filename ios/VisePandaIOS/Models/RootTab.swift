import Foundation

enum RootTab: String, CaseIterable, Identifiable, Codable {
    case trips
    case explore
    case chat
    case tools
    case me

    var id: String { rawValue }

    var title: String {
        switch self {
        case .trips:
            "Trips"
        case .explore:
            "Explore"
        case .chat:
            "Chat"
        case .tools:
            "Tools"
        case .me:
            "Me"
        }
    }

    var systemImage: String {
        switch self {
        case .trips:
            "map"
        case .explore:
            "safari"
        case .chat:
            "bubble.left"
        case .tools:
            "wrench.adjustable"
        case .me:
            "person"
        }
    }

    static let leftOfCenter: [RootTab] = [.trips, .explore]
    static let rightOfCenter: [RootTab] = [.tools, .me]
}
