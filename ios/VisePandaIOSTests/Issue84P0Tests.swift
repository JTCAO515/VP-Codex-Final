import XCTest
@testable import VisePandaIOS

final class Issue84P0Tests: XCTestCase {
    func testSupportedLanguagesExposeSevenBackendCodes() {
        XCTAssertEqual(SupportedLanguages.all.map(\.code), ["en", "zh", "ar", "es", "fr", "ja", "ko"])
        XCTAssertEqual(SupportedLanguages.byCode("ja").speechLocale, "ja-JP")
        XCTAssertEqual(SupportedLanguages.byCode("missing").code, "en")
    }

    func testTripCompletenessUsesRealTripState() {
        let completeTrip = TripState(
            summary: TripSummary(
                title: "Shanghai",
                durationDays: 1,
                pace: .balanced,
                travelerStyle: "solo",
                destinations: ["Shanghai"],
                confidence: .refined
            ),
            days: [
                TripDay(
                    day: 1,
                    city: "Shanghai",
                    pace: .balanced,
                    blocks: [
                        TripBlock(time: .morning, title: "Yu Garden", description: "Walk the old town."),
                        TripBlock(time: .afternoon, title: "Museum", description: "Indoor stop."),
                        TripBlock(time: .evening, title: "Bund", description: "Night view.")
                    ],
                    food: ["shengjian"],
                    stay: "People's Square",
                    transport: "Metro",
                    note: "",
                    status: .new
                )
            ],
            alerts: [
                ButlerAlert(type: .payment, priority: .medium, title: "Payment ready", body: "", action: "", done: true),
                ButlerAlert(type: .visa, priority: .medium, title: "Visa ready", body: "", action: "", done: true)
            ],
            lastUpdatedReason: "test"
        )

        XCTAssertEqual(TripCompleteness.calculateTripCompleteness(completeTrip).score, 100)
        XCTAssertEqual(TripCompleteness.calculateDayCompleteness(completeTrip.days[0]), 100)
    }

    func testTimelineBuildsNowNextLaterFromFirstDay() {
        let trip = TripState(
            summary: TripSummary(
                title: "Shanghai",
                durationDays: 1,
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
                        TripBlock(time: .morning, title: "A", description: ""),
                        TripBlock(time: .afternoon, title: "B", description: ""),
                        TripBlock(time: .evening, title: "C", description: "")
                    ],
                    food: [],
                    stay: "",
                    transport: "",
                    note: "",
                    status: nil
                )
            ],
            alerts: [],
            lastUpdatedReason: "test"
        )

        XCTAssertEqual(TripTimeline.buildTimeline(trip).map(\.position), [.now, .next, .later])
    }
}
