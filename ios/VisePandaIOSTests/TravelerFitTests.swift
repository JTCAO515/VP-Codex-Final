import XCTest
@testable import VisePandaIOS

final class TravelerFitTests: XCTestCase {
    func testNightFitDerivesFromLateOpeningHours() throws {
        let poi = try decodePoi("""
        {
          "id": "night-1",
          "name": "Late Restaurant",
          "type": "Food;Restaurant",
          "opentime_week": "10:00-22:30"
        }
        """)

        XCTAssertEqual(poi.travelerFit?.nightFit, true)
    }

    func testIndoorMallDerivesRainAndLuggageFit() throws {
        let poi = try decodePoi("""
        {
          "id": "mall-1",
          "name": "City Mall",
          "type": "Shopping;Mall",
          "business_area": "Xujiahui"
        }
        """)

        XCTAssertEqual(poi.travelerFit?.rainyDayFit, true)
        XCTAssertEqual(poi.travelerFit?.luggageFit, true)
    }

    func testOutdoorGardenDerivesWatchOutWithoutInventingOtherFields() throws {
        let poi = try decodePoi("""
        {
          "id": "garden-1",
          "name": "Old Garden",
          "type": "Attraction;Garden"
        }
        """)

        XCTAssertEqual(poi.travelerFit?.rainyDayFit, false)
        XCTAssertEqual(poi.travelerFit?.luggageFit, false)
        XCTAssertEqual(poi.travelerFit?.watchOut, "Best in dry weather.")
        XCTAssertNil(poi.travelerFit?.paymentFriendliness)
        XCTAssertNil(poi.travelerFit?.routeFit)
    }

    func testPaymentAndLanguageOnlyDeriveFromExplicitSignals() throws {
        let poi = try decodePoi("""
        {
          "id": "friendly-1",
          "name": "Friendly Cafe",
          "type": "Food;Cafe",
          "editorial": {
            "tags": ["English menu", "Foreign card accepted"]
          }
        }
        """)

        XCTAssertEqual(poi.travelerFit?.languageDifficulty, "Lower")
        XCTAssertEqual(poi.travelerFit?.paymentFriendliness, "Card accepted")
    }

    func testMatchesKebabCaseCuratedTagsLikeScriptsCuratedSeedsUse() throws {
        // scripts/curated-seeds/*.sql tags are kebab-case (e.g. "english-menu"),
        // not space-separated — this must match the same as the space form.
        let poi = try decodePoi("""
        {
          "id": "curated-1",
          "name": "Four Seasons Beijing",
          "type": "Food;Restaurant",
          "editorial": {
            "tags": ["english-menu"]
          }
        }
        """)

        XCTAssertEqual(poi.travelerFit?.languageDifficulty, "Lower")
    }

    func testTravelerFitIsNilWhenNoRuleHasData() throws {
        let poi = try decodePoi("""
        {
          "id": "plain-1",
          "name": "Plain Place"
        }
        """)

        XCTAssertNil(poi.travelerFit)
    }

    private func decodePoi(_ json: String) throws -> ExploreAmapPoi {
        try JSONDecoder().decode(ExploreAmapPoi.self, from: Data(json.utf8))
    }
}
