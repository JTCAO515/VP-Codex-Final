package space.go2china.visepanda.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import space.go2china.visepanda.data.model.Phrase
import space.go2china.visepanda.data.model.StaticTranslateData
import space.go2china.visepanda.data.model.TranslateRequest
import space.go2china.visepanda.data.model.TranslateResult
import space.go2china.visepanda.data.remote.TranslateApiService

interface TranslateRepository {
    suspend fun translateText(text: String, from: String = "en", to: String = "zh"): Result<TranslateResult>
    suspend fun getPhrases(): List<Phrase>
}

@Singleton
class LiveTranslateRepository @Inject constructor(
    private val translateApiService: TranslateApiService,
) : TranslateRepository {

    override suspend fun translateText(text: String, from: String, to: String): Result<TranslateResult> {
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

    override suspend fun getPhrases(): List<Phrase> {
        return StaticTranslateData.phrases
    }
}
