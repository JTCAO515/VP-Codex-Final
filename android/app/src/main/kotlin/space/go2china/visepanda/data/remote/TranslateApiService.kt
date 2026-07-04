package space.go2china.visepanda.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import space.go2china.visepanda.data.model.TranslateRequest
import space.go2china.visepanda.data.model.TranslateResponse

interface TranslateApiService {
    @POST("api/translate/text")
    suspend fun translateText(
        @Body request: TranslateRequest
    ): TranslateResponse
}
