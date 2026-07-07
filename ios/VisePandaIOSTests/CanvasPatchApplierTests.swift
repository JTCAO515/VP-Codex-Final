import XCTest
@testable import VisePandaIOS

final class CanvasPatchApplierTests: XCTestCase {
    func testNilDaysKeepsExistingTripDaysAndMissingFieldsUnchanged() {
        let current = makeTrip()
        let patch = makePatch(days: nil, tripSummary: nil, butlerAlerts: nil)

        let result = CanvasPatchApplier.apply(current: current, patch: patch)

        XCTAssertEqual(result.days, current.days)
        XCTAssertEqual(result.summary, current.summary)
        XCTAssertEqual(result.alerts, current.alerts)
    }

    func testEmptyDaysKeepsExistingTripDays() {
        let current = makeTrip()
        let patch = makePatch(days: [])

        let result = CanvasPatchApplier.apply(current: current, patch: patch)

        XCTAssertEqual(result.days, current.days)
    }

    func testNonEmptyDaysReplaceTripDays() {
        let current = makeTrip()
        let replacement = TripDay(
            day: 3,
            city: "Hangzhou",
            pace: .relaxed,
            blocks: [
                TripBlock(time: .morning, title: "West Lake", description: "Walk by the lake.")
            ],
            food: ["Longjing tea"],
            stay: "West Lake hotel",
            transport: "Train",
            note: "New day",
            status: .new
        )
        let patch = makePatch(days: [replacement])

        let result = CanvasPatchApplier.apply(current: current, patch: patch)

        XCTAssertEqual(result.days, [replacement])
    }

    func testSummaryAndAlertsMergeOnlyWhenPresent() {
        let current = makeTrip()
        let alert = ButlerAlert(type: .weather, priority: .low, title: "Rain", body: "Bring an umbrella.", action: "Pack umbrella", done: false)
        let patch = makePatch(
            tripSummary: TripSummaryPatch(title: "Shanghai refined", durationDays: nil, pace: nil, travelerStyle: nil, destinations: nil, confidence: .refined),
            butlerAlerts: [alert]
        )

        let result = CanvasPatchApplier.apply(current: current, patch: patch)

        XCTAssertEqual(result.summary.title, "Shanghai refined")
        XCTAssertEqual(result.summary.durationDays, current.summary.durationDays)
        XCTAssertTrue(result.alerts.contains(alert))
        XCTAssertTrue(result.alerts.contains(current.alerts[0]))
    }

    private func makePatch(
        days: [TripDay]? = nil,
        tripSummary: TripSummaryPatch? = nil,
        butlerAlerts: [ButlerAlert]? = nil
    ) -> CanvasPatch {
        CanvasPatch(
            intent: .adjustTrip,
            assistantMessage: "Updated.",
            assistantResponse: nil,
            tripSummary: tripSummary,
            days: days,
            butlerAlerts: butlerAlerts,
            reason: "test"
        )
    }

    private func makeTrip() -> TripState {
        TripState(
            summary: TripSummary(
                title: "Shanghai sample",
                durationDays: 2,
                pace: .balanced,
                travelerStyle: "solo",
                destinations: ["Shanghai"],
                confidence: .draft
            ),
            days: [
                TripDay(
                    day: 1,
                    city: "Shanghai",
                    pace: .balanced,
                    blocks: [
                        TripBlock(time: .morning, title: "Yu Garden", description: "Walk the old town.")
                    ],
                    food: ["Noodles"],
                    stay: "People's Square",
                    transport: "Metro",
                    note: "Keep it light.",
                    status: .new
                ),
                TripDay(
                    day: 2,
                    city: "Shanghai",
                    pace: .balanced,
                    blocks: [
                        TripBlock(time: .afternoon, title: "Museum", description: "Indoor stop.")
                    ],
                    food: ["Tea"],
                    stay: "People's Square",
                    transport: "Walking",
                    note: "Rain option.",
                    status: .revised
                )
            ],
            alerts: [
                ButlerAlert(type: .payment, priority: .medium, title: "Payment", body: "Set up wallet.", action: "Open tools", done: false)
            ],
            lastUpdatedReason: "seed"
        )
    }
}
