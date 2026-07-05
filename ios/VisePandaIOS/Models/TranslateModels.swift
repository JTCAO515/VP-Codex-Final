import Foundation

struct TranslateResult: Equatable {
    var translation: String
    var pinyin: String
}

struct TranslateOcrRequest: Codable {
    var imageBase64: String
    var mimeType: String
}

struct TranslateOcrResponse: Codable {
    var ok: Bool
    var provider: String?
    var model: String?
    var text: String?
    var error: String?
}

struct TranslateSttRequest: Codable {
    var audioBase64: String
    var mimeType: String
    var language: String
}

struct TranslateSttResponse: Codable {
    var ok: Bool
    var provider: String?
    var model: String?
    var text: String?
    var language: String?
    var error: String?
}

struct Phrase: Identifiable, Hashable {
    var id: String { "\(category)-\(english)" }
    var category: String
    var english: String
    var chinese: String
    var pinyin: String
}

enum StaticTranslateData {
    static let phrases = [
        Phrase(category: "Greeting", english: "Hello", chinese: "你好", pinyin: "Nǐ hǎo"),
        Phrase(category: "Greeting", english: "Thank you", chinese: "谢谢", pinyin: "Xièxiè"),
        Phrase(category: "Greeting", english: "Goodbye", chinese: "再见", pinyin: "Zàijiàn"),
        Phrase(category: "Transportation", english: "Where is the subway station?", chinese: "地铁站在哪里？", pinyin: "Dìtiě zhàn zài nǎlǐ?"),
        Phrase(category: "Transportation", english: "I want to go to...", chinese: "我想去...", pinyin: "Wǒ xiǎng qù..."),
        Phrase(category: "Dining", english: "Water, please", chinese: "请给我水", pinyin: "Qǐng gěi wǒ shuǐ"),
        Phrase(category: "Dining", english: "Check, please", chinese: "结账", pinyin: "Jiézhàng"),
        Phrase(category: "Dining", english: "Not spicy", chinese: "不要辣", pinyin: "Bú yào là"),
        Phrase(category: "Emergency", english: "Help!", chinese: "救命！", pinyin: "Jiùmìng!"),
        Phrase(category: "Emergency", english: "Call the police", chinese: "报警", pinyin: "Bàojǐng")
    ]
}
