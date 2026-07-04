import Foundation

enum CanvasPatchApplier {
    static func apply(current: TripState, patch: CanvasPatch) -> TripState {
        var summary = current.summary
        if let tripSummary = patch.tripSummary {
            summary.title = tripSummary.title ?? summary.title
            summary.durationDays = tripSummary.durationDays ?? summary.durationDays
            summary.pace = tripSummary.pace ?? summary.pace
            summary.travelerStyle = tripSummary.travelerStyle ?? summary.travelerStyle
            summary.destinations = tripSummary.destinations ?? summary.destinations
            summary.confidence = tripSummary.confidence ?? summary.confidence
        }

        var alertMap = Dictionary(uniqueKeysWithValues: current.alerts.map { ($0.id, $0) })
        for alert in patch.butlerAlerts ?? [] {
            alertMap[alert.id] = alert
        }

        return TripState(
            summary: summary,
            days: patch.days ?? current.days,
            alerts: Array(alertMap.values).sorted { $0.title < $1.title },
            lastUpdatedReason: patch.reason
        )
    }
}
