import Foundation

// Ported from the Web reference implementation (lib/canvas/diffTripState.ts,
// see Issue #124 comment on Issue #134's audit finding) rather than
// redesigned from scratch — same day-content-key / alert-key diff algorithm,
// already proven in production on the Web client.
struct ChangeDigestEntry: Codable, Equatable, Identifiable {
    enum Kind: String, Codable {
        case added
        case revised
        case removed
        case alert
    }

    var kind: Kind
    var dayNumber: Int?
    var label: String

    var id: String { "\(kind.rawValue)-\(dayNumber.map(String.init) ?? "none")-\(label)" }
}

enum ChangeDigest {
    private static func dayContentKey(_ day: TripDay) -> String {
        let blocksKey = day.blocks
            .map { "\($0.time.rawValue)|\($0.title)|\($0.description)" }
            .joined(separator: ";")
        return [blocksKey, day.food.joined(separator: ","), day.stay, day.transport, day.note].joined(separator: "||")
    }

    private static func dayLabel(_ day: TripDay) -> String {
        let detail = day.blocks.first?.title ?? day.city
        return "\(detail) (\(day.city))"
    }

    private static func alertKey(_ alert: ButlerAlert) -> String {
        "\(alert.type.rawValue):\(alert.title)"
    }

    /// Pure day-level + alert-level diff between two TripState snapshots, used
    /// to render the post-patch "Trip updated" summary. Returns an empty
    /// array when the patch made no visible change (the summary should not
    /// render in that case) — mirrors diffTripState.ts's contract exactly.
    static func compute(previous: TripState, next: TripState) -> [ChangeDigestEntry] {
        var entries: [ChangeDigestEntry] = []

        let previousDays = Dictionary(uniqueKeysWithValues: previous.days.map { ($0.day, $0) })
        let nextDays = Dictionary(uniqueKeysWithValues: next.days.map { ($0.day, $0) })

        for day in next.days {
            guard let previousDay = previousDays[day.day] else {
                entries.append(ChangeDigestEntry(kind: .added, dayNumber: day.day, label: "Day \(day.day) added · \(dayLabel(day))"))
                continue
            }
            if dayContentKey(previousDay) != dayContentKey(day) {
                entries.append(ChangeDigestEntry(kind: .revised, dayNumber: day.day, label: "Day \(day.day) updated · \(dayLabel(day))"))
            }
        }

        for day in previous.days where nextDays[day.day] == nil {
            entries.append(ChangeDigestEntry(kind: .removed, dayNumber: day.day, label: "Day \(day.day) removed · \(dayLabel(day))"))
        }

        let previousAlertKeys = Set(previous.alerts.map(alertKey))
        for alert in next.alerts where !previousAlertKeys.contains(alertKey(alert)) {
            entries.append(ChangeDigestEntry(kind: .alert, dayNumber: nil, label: "New reminder · \(alert.title)"))
        }

        return entries.sorted { ($0.dayNumber ?? Int.max) < ($1.dayNumber ?? Int.max) }
    }
}
