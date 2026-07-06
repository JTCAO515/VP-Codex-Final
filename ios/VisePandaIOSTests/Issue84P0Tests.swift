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

    func testProfileSyncStatusCoversP1States() {
        XCTAssertEqual(ProfileSyncStatus.evaluate(isSignedIn: false, isLoading: false, hasError: false, hasEntries: false).title, "Not signed in")
        XCTAssertEqual(ProfileSyncStatus.evaluate(isSignedIn: true, isLoading: true, hasError: false, hasEntries: false).title, "Syncing")
        XCTAssertEqual(ProfileSyncStatus.evaluate(isSignedIn: true, isLoading: false, hasError: true, hasEntries: false).title, "Sync failed")
        XCTAssertEqual(ProfileSyncStatus.evaluate(isSignedIn: true, isLoading: false, hasError: false, hasEntries: true).title, "Synced")
        XCTAssertEqual(ProfileSyncStatus.evaluate(isSignedIn: true, isLoading: false, hasError: false, hasEntries: false).title, "Not synced")
    }

    func testCanvasPatchDecodesAffectedDays() throws {
        let data = """
        {
          "intent": "adjust_trip",
          "assistantMessage": "Updated Day 2.",
          "affectedDays": [2, 3],
          "generationStage": "complete",
          "reason": "test"
        }
        """.data(using: .utf8)!

        let patch = try JSONDecoder.visePanda.decode(CanvasPatch.self, from: data)
        XCTAssertEqual(patch.affectedDays, [2, 3])
        XCTAssertEqual(patch.generationStage, "complete")
    }

    func testChatRequestEncodesCompleteSkeletonFor() throws {
        let trip = TripState(
            summary: TripSummary(
                title: "Beijing",
                durationDays: 1,
                pace: .balanced,
                travelerStyle: "solo",
                destinations: ["Beijing"],
                confidence: .draft
            ),
            days: [
                TripDay(
                    day: 1,
                    city: "Beijing",
                    pace: .balanced,
                    blocks: [],
                    food: ["noodles"],
                    stay: "Dongcheng",
                    transport: "Metro",
                    note: "Skeleton day",
                    status: .new
                )
            ],
            alerts: [],
            lastUpdatedReason: "skeleton"
        )

        let request = ButlerChatRequest(
            message: "",
            trip: trip,
            messages: [],
            preferenceProfile: nil,
            completeSkeletonFor: trip
        )
        let object = try JSONSerialization.jsonObject(with: JSONEncoder.visePanda.encode(request)) as? [String: Any]

        XCTAssertEqual(object?["message"] as? String, "")
        XCTAssertNotNil(object?["completeSkeletonFor"])
    }

    @MainActor
    func testTripStoreConsumesPendingTripDay() {
        let store = TripStore()

        store.openTripDay(2)

        XCTAssertEqual(store.selectedTab, .trips)
        XCTAssertEqual(store.consumePendingTripDayNumber(), 2)
        XCTAssertNil(store.consumePendingTripDayNumber())
    }
}
