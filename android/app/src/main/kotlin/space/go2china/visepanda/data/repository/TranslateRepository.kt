package space.go2china.visepanda.data.repository

import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

data class TranslateTextRequest(
    val text: String,
    val from: String = "en",
    val to: String = "zh"
)

data class TranslateTextResponse(
    val ok: Boolean,
    val provider: String? = null,
    val model: String? = null,
    val from: String? = null,
    val to: String? = null,
    val translatedText: String? = null,
    val error: String? = null
)

interface TranslateApiService {
    @POST("api/translate/text")
    suspend fun translateText(@Body request: TranslateTextRequest): TranslateTextResponse
}

@Singleton
class TranslateRepository @Inject constructor(
    private val retrofit: Retrofit
) {
    private val apiService by lazy { retrofit.create(TranslateApiService::class.java) }

    suspend fun translate(text: String, from: String = "en", to: String = "zh"): Result<String> {
        return runCatching {
            val response = apiService.translateText(TranslateTextRequest(text, from, to))
            if (response.ok && response.translatedText != null) {
                response.translatedText
            } else {
                throw Exception(response.error ?: "Translation failed")
            }
        }
    }
}
