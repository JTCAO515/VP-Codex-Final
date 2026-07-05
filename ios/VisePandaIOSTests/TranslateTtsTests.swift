import XCTest
@testable import VisePandaIOS

final class TranslateTtsTests: XCTestCase {
    func testSupportedLanguagesExposeBackendTtsNames() {
        XCTAssertEqual(SupportedLanguages.byCode("zh").ttsLanguageName, "Chinese")
        XCTAssertEqual(SupportedLanguages.byCode("ja").ttsLanguageName, "Japanese")
        XCTAssertEqual(SupportedLanguages.byCode("ko").speechLocale, "ko-KR")
    }

    func testDashscopeHttpAudioUrlIsUpgradedToHttps() {
        let url = TranslateTtsURL.playableURL(from: "http://example.com/audio.wav?signature=abc")
        XCTAssertEqual(url?.absoluteString, "https://example.com/audio.wav?signature=abc")
    }

    func testExistingHttpsAudioUrlIsKept() {
        let url = TranslateTtsURL.playableURL(from: "https://example.com/audio.wav")
        XCTAssertEqual(url?.absoluteString, "https://example.com/audio.wav")
    }
}
