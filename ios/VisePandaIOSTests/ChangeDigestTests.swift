import XCTest
@testable import VisePandaIOS

final class ChangeDigestTests: XCTestCase {
    private func day(_ number: Int, city: String = "Beijing", title: String = "Tiananmen Square") -> TripDay {
        TripDay(
            day: number,
            city: city,
            pace: .balanced,
            blocks: [TripBlock(time: .morning, title: title, description: "Arrive early.")],
            food: [],
            stay: "",
            transport: "",
            note: ""
        )
    }

    private func trip(days: [TripDay], alerts: [ButlerAlert] = []) -> TripState {
        TripState(
            summary: TripSummary(title: "Trip", durationDays: days.count, pace: .balanced, travelerStyle: "solo", destinations: ["Beijing"], confidence: .refined),
            days: days,
            alerts: alerts,
            lastUpdatedReason: "test"
        )
    }

    func testNoChangeReturnsEmpty() {
        let previous = trip(days: [day(1)])
        let next = trip(days: [day(1)])
        XCTAssertEqual(ChangeDigest.compute(previous: previous, next: next), [])
    }

    func testAddedDay() {
        let previous = trip(days: [day(1)])
        let next = trip(days: [day(1), day(2, city: "Suzhou", title: "Humble Administrator's Garden")])
        let entries = ChangeDigest.compute(previous: previous, next: next)
        XCTAssertEqual(entries.count, 1)
        XCTAssertEqual(entries[0].kind, .added)
        XCTAssertEqual(entries[0].dayNumber, 2)
        XCTAssertTrue(entries[0].label.contains("Day 2 added"))
    }

    func testRevisedDay() {
        let previous = trip(days: [day(1, title: "Old Block")])
        let next = trip(days: [day(1, title: "New Block")])
        let entries = ChangeDigest.compute(previous: previous, next: next)
        XCTAssertEqual(entries.count, 1)
        XCTAssertEqual(entries[0].kind, .revised)
        XCTAssertEqual(entries[0].dayNumber, 1)
    }

    func testRemovedDay() {
        let previous = trip(days: [day(1), day(2)])
        let next = trip(days: [day(1)])
        let entries = ChangeDigest.compute(previous: previous, next: next)
        XCTAssertEqual(entries.count, 1)
        XCTAssertEqual(entries[0].kind, .removed)
        XCTAssertEqual(entries[0].dayNumber, 2)
    }

    func testNewAlert() {
        let alert = ButlerAlert(type: .visa, priority: .high, title: "Check entry rules", body: "...", action: "Review")
        let previous = trip(days: [day(1)], alerts: [])
        let next = trip(days: [day(1)], alerts: [alert])
        let entries = ChangeDigest.compute(previous: previous, next: next)
        XCTAssertEqual(entries.count, 1)
        XCTAssertEqual(entries[0].kind, .alert)
        XCTAssertNil(entries[0].dayNumber)
    }

    func testEntriesSortedByDayNumberWithAlertsLast() {
        let alert = ButlerAlert(type: .visa, priority: .high, title: "Check entry rules", body: "...", action: "Review")
        let previous = trip(days: [day(2)], alerts: [])
        let next = trip(days: [day(1), day(2)], alerts: [alert])
        let entries = ChangeDigest.compute(previous: previous, next: next)
        XCTAssertEqual(entries.map(\.dayNumber), [1, nil])
    }
}
