package space.go2china.visepanda.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import space.go2china.visepanda.data.model.CanvasPatch
import space.go2china.visepanda.data.model.TripState

data class ButlerChatRequest(
    val message: String,
    val trip: TripState,
    val messages: List<RemoteChatMessage>,
    val poi: String? = null,
)

data class RemoteChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val createdAt: String? = null,
)

data class ButlerChatResponse(
    val ok: Boolean,
    val mode: String? = null,
    val modelLabel: String? = null,
    val patch: CanvasPatch,
    val suggestions: List<String> = emptyList(),
    val fallbackReason: String? = null,
)

interface ButlerApiService {
    @POST("api/chat")
    suspend fun chat(@Body request: ButlerChatRequest): ButlerChatResponse
}
