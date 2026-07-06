import XCTest
@testable import VisePandaIOS

final class TranslateTtsTests: XCTestCase {
    func testSupportedLanguagesExposeTtsNames() {
        XCTAssertEqual(SupportedLanguages.byCode("zh").ttsLanguageName, "Chinese")
        XCTAssertEqual(SupportedLanguages.byCode("ja").ttsLanguageName, "Japanese")
    }

    func testTtsUrlUpgradesCleartextDashscopeUrl() {
        let url = TranslateTtsURL(rawValue: "http://example.com/audio.mp3?sig=1")
        XCTAssertEqual(url.httpsURL?.absoluteString, "https://example.com/audio.mp3?sig=1")
    }
}
