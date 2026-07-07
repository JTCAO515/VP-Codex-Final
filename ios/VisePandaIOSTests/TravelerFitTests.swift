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

    // Issue #154: real Amap `type`/`address`/`business_area` fields are plain
    // Chinese, with no curated English editorial tags. These cases lock in
    // that the Chinese keyword branches added alongside the English ones
    // actually fire on that raw data, not just on curated English text.

    func testChineseMuseumTypeDerivesRainyDayAndFirstTimerFit() throws {
        let poi = try decodePoi("""
        {
          "id": "cn-museum-1",
          "name": "兵马俑博物馆",
          "type": "风景名胜;博物馆;世界遗产",
          "address": "西安市临潼区秦陵北路",
          "biz_ext": { "rating": "4.8" }
        }
        """)

        XCTAssertEqual(poi.travelerFit?.rainyDayFit, true)
        XCTAssertEqual(poi.travelerFit?.firstTimerFit, true)
    }

    func testChineseHotelTypeDerivesRainyDayAndLuggageFit() throws {
        let poi = try decodePoi("""
        {
          "id": "cn-hotel-1",
          "name": "广州文华东方酒店",
          "type": "酒店;五星级",
          "address": "广州市天河区兴盛路389号"
        }
        """)

        XCTAssertEqual(poi.travelerFit?.rainyDayFit, true)
        XCTAssertEqual(poi.travelerFit?.luggageFit, true)
    }

    func testChineseParkAddressDerivesOutdoorRainyDayAndLuggageFit() throws {
        let poi = try decodePoi("""
        {
          "id": "cn-park-1",
          "name": "老公园",
          "type": "风景名胜;公园;公园",
          "address": "某市某区人民公园"
        }
        """)

        XCTAssertEqual(poi.travelerFit?.rainyDayFit, false)
        XCTAssertEqual(poi.travelerFit?.luggageFit, false)
    }

    func testChineseCrowdedKeywordDerivesCrowdRisk() throws {
        let poi = try decodePoi("""
        {
          "id": "cn-crowd-1",
          "name": "网红打卡地",
          "type": "餐饮服务;中餐厅;中餐厅",
          "editorial": {
            "summary": "本地人也常来的网红打卡地，周末人多。"
          }
        }
        """)

        XCTAssertEqual(poi.travelerFit?.crowdRisk, "High")
    }

    func testChineseCashOnlyAndEnglishMenuEditorialDeriveExplicitSignals() throws {
        let poi = try decodePoi("""
        {
          "id": "cn-editorial-1",
          "name": "老字号小店",
          "type": "美食;面馆",
          "editorial": {
            "tags": ["仅收现金", "英文菜单"]
          }
        }
        """)

        XCTAssertEqual(poi.travelerFit?.paymentFriendliness, "Cash only")
        XCTAssertEqual(poi.travelerFit?.languageDifficulty, "Lower")
    }

    func testChineseAirportTypeDerivesLuggageFitWithoutEnglishKeywords() throws {
        let poi = try decodePoi("""
        {
          "id": "cn-airport-1",
          "name": "白云机场附近酒店",
          "type": "交通设施服务;机场相关;机场",
          "address": "广州市花都区机场路"
        }
        """)

        XCTAssertEqual(poi.travelerFit?.luggageFit, true)
    }

    private func decodePoi(_ json: String) throws -> ExploreAmapPoi {
        try JSONDecoder().decode(ExploreAmapPoi.self, from: Data(json.utf8))
    }
}
