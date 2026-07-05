package space.go2china.visepanda.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import space.go2china.visepanda.data.model.TranslateRequest
import space.go2china.visepanda.data.model.TranslateResponse
import space.go2china.visepanda.data.model.OcrRequest
import space.go2china.visepanda.data.model.OcrResponse
import space.go2china.visepanda.data.model.SttRequest
import space.go2china.visepanda.data.model.SttResponse

interface TranslateApiService {
    @POST("api/translate/text")
    suspend fun translateText(
        @Body request: TranslateRequest
    ): TranslateResponse

    @POST("api/translate/ocr")
    suspend fun translateOcr(
        @Body request: OcrRequest
    ): OcrResponse

    @POST("api/translate/stt")
    suspend fun translateStt(
        @Body request: SttRequest
    ): SttResponse
}
