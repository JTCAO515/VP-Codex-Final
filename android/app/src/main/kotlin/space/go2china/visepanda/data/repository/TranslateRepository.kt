package space.go2china.visepanda.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import space.go2china.visepanda.data.model.Phrase
import space.go2china.visepanda.data.model.StaticTranslateData
import space.go2china.visepanda.data.model.OcrRequest
import space.go2china.visepanda.data.model.SttRequest
import space.go2china.visepanda.data.model.TranslateRequest
import space.go2china.visepanda.data.model.TranslateResult
import space.go2china.visepanda.data.model.TtsRequest
import space.go2china.visepanda.data.remote.TranslateApiService

const val MOCK_FALLBACK_ON_FAILURE = false

interface TranslateRepository {
    suspend fun translateText(text: String, from: String = "en", to: String = "zh"): Result<TranslateResult>
    suspend fun translateOcr(imageBase64: String, mimeType: String = "image/jpeg"): Result<String>
    suspend fun translateStt(audioBase64: String, mimeType: String = "audio/mpeg", language: String = "zh"): Result<String>
    /** Returns a remote audio URL synthesized by Qwen TTS, or failure if unavailable. */
    suspend fun translateTts(text: String, ttsLanguageName: String): Result<String>
    suspend fun getPhrases(): List<Phrase>
}

@Singleton
class LiveTranslateRepository @Inject constructor(
    private val translateApiService: TranslateApiService,
) : TranslateRepository {

    override suspend fun translateText(text: String, from: String, to: String): Result<TranslateResult> {
        if (MOCK_FALLBACK_ON_FAILURE) {
            val mockRes = when (text) {
                "Welcome to Beijing" -> TranslateResult(
                    translation = "欢迎来到北京",
                    pinyin = "Huānyíng lái dào Běijīng"
                )
                "Where is the subway station?" -> TranslateResult(
                    translation = "地铁站在哪里？",
                    pinyin = "Dìtiězhàn zài nǎlǐ?"
                )
                else -> TranslateResult(
                    translation = "翻译成功 [Mock]",
                    pinyin = "Fānyì chénggōng"
                )
            }
            return Result.success(mockRes)
        }
        return runCatching {
            val response = translateApiService.translateText(
                TranslateRequest(text = text, from = from, to = to)
            )
            if (response.ok && response.translation != null) {
                TranslateResult(
                    translation = response.translation,
                    pinyin = response.pinyin.orEmpty()
                )
            } else {
                throw Exception(response.error ?: "Translation response is not ok")
            }
        }
    }

    override suspend fun translateOcr(imageBase64: String, mimeType: String): Result<String> {
        if (MOCK_FALLBACK_ON_FAILURE) {
            return Result.success("Welcome to Beijing")
        }
        return runCatching {
            val response = translateApiService.translateOcr(
                OcrRequest(imageBase64 = imageBase64, mimeType = mimeType)
            )
            if (response.ok && !response.text.isNullOrBlank()) {
                response.text
            } else {
                throw Exception(response.error ?: "OCR text is blank")
            }
        }
    }

    override suspend fun translateStt(audioBase64: String, mimeType: String, language: String): Result<String> {
        if (MOCK_FALLBACK_ON_FAILURE) {
            return Result.success("Where is the subway station?")
        }
        return runCatching {
            val response = translateApiService.translateStt(
                SttRequest(audioBase64 = audioBase64, mimeType = mimeType, language = language)
            )
            if (response.ok && !response.text.isNullOrBlank()) {
                response.text
            } else {
                throw Exception(response.error ?: "STT text is blank")
            }
        }
    }

    override suspend fun translateTts(text: String, ttsLanguageName: String): Result<String> {
        return runCatching {
            val response = translateApiService.translateTts(
                TtsRequest(text = text, language = ttsLanguageName)
            )
            if (response.ok && !response.audioUrl.isNullOrBlank()) {
                // Dashscope returns plain http:// URLs; Android blocks cleartext
                // traffic by default (targetSdk 34), which would silently fail
                // MediaPlayer playback. The signed OSS URL works over https too.
                response.audioUrl.replaceFirst("http://", "https://")
            } else {
                throw Exception(response.error ?: "TTS audio URL is blank")
            }
        }
    }

    override suspend fun getPhrases(): List<Phrase> {
        return StaticTranslateData.phrases
    }
}
