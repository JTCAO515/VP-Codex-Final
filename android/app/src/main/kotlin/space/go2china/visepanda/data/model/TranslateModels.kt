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
 * Request body for OCR translation, matching next.js `/api/translate/ocr`
 */
data class OcrRequest(
    val imageBase64: String,
    val mimeType: String = "image/jpeg",
)

/**
 * Response body for OCR translation, matching next.js `/api/translate/ocr`
 */
data class OcrResponse(
    val ok: Boolean,
    val provider: String? = null,
    val model: String? = null,
    val text: String? = null,
    val error: String? = null,
)

/**
 * Request body for STT translation, matching next.js `/api/translate/stt`
 */
data class SttRequest(
    val audioBase64: String,
    val mimeType: String = "audio/mpeg",
    val language: String = "zh",
)

/**
 * Response body for STT translation, matching next.js `/api/translate/stt`
 */
data class SttResponse(
    val ok: Boolean,
    val provider: String? = null,
    val model: String? = null,
    val text: String? = null,
    val language: String? = null,
    val error: String? = null,
)


/**
 * Request body for TTS synthesis, matching next.js `/api/translate/tts`
 */
data class TtsRequest(
    val text: String,
    val language: String = "Chinese",
    val voice: String = "Cherry",
)

/**
 * Response body for TTS synthesis, matching next.js `/api/translate/tts`
 */
data class TtsResponse(
    val ok: Boolean,
    val provider: String? = null,
    val model: String? = null,
    val audioUrl: String? = null,
    val expiresAt: String? = null,
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
 * A language the backend `/api/translate/text` and `/api/translate/tts`
 * endpoints support. `ttsLanguageName` mirrors the `language_type` values
 * Qwen TTS expects, which differ slightly from the translate endpoint's
 * `LANGUAGE_NAMES` (e.g. "Chinese" not "Simplified Chinese").
 */
data class SupportedLanguage(
    val code: String,
    val displayName: String,
    val ttsLanguageName: String,
)

object SupportedLanguages {
    val all = listOf(
        SupportedLanguage("en", "English", "English"),
        SupportedLanguage("zh", "中文", "Chinese"),
        SupportedLanguage("ar", "العربية", "Arabic"),
        SupportedLanguage("es", "Español", "Spanish"),
        SupportedLanguage("fr", "Français", "French"),
        SupportedLanguage("ja", "日本語", "Japanese"),
        SupportedLanguage("ko", "한국어", "Korean"),
    )

    fun byCode(code: String): SupportedLanguage = all.first { it.code == code }
}

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
