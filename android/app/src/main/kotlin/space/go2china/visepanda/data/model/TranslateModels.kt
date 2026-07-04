package space.go2china.visepanda.data.model

/**
 * Request body for text translation, matching next.js `/api/translate/text`
 */
data class TranslateRequest(
    val text: String,
    val from: String = "en",
    val to: String = "zh",
)

/**
 * Response body for text translation, matching next.js `/api/translate/text`
 */
data class TranslateResponse(
    val ok: Boolean,
    val provider: String? = null,
    val model: String? = null,
    val from: String? = null,
    val to: String? = null,
    val translation: String? = null,
    val pinyin: String? = null,
    val error: String? = null,
)

/**
 * Success translation result passed to the UI layer
 */
data class TranslateResult(
    val translation: String,
    val pinyin: String,
)

/**
 * Phrase object for the static dictionary
 */
data class Phrase(
    val category: String,
    val english: String,
    val chinese: String,
    val pinyin: String,
)

/**
 * Local phrasebook database representing common tourist scenarios.
 */
object StaticTranslateData {
    val phrases = listOf(
        Phrase("Greeting", "Hello", "你好", "Nǐ hǎo"),
        Phrase("Greeting", "Thank you", "谢谢", "Xièxiè"),
        Phrase("Greeting", "Goodbye", "再见", "Zàijiàn"),
        Phrase("Transportation", "Where is the subway station?", "地铁站在哪里？", "Dìtiě zhàn zài nǎlǐ?"),
        Phrase("Transportation", "I want to go to...", "我想去...", "Wǒ xiǎng qù..."),
        Phrase("Dining", "Water, please", "请给我水", "Qǐng gěi wǒ shuǐ"),
        Phrase("Dining", "Check, please", "结账", "Jiézhàng"),
        Phrase("Dining", "Not spicy", "不要辣", "Bú yào là"),
        Phrase("Emergency", "Help!", "救命！", "Jiùmìng!"),
        Phrase("Emergency", "Call the police", "报警", "Bàojǐng"),
    )
}
