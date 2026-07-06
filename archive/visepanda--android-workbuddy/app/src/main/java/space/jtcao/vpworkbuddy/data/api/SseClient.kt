package space.jtcao.vpworkbuddy.data.api

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.closeQuietly
import space.jtcao.vpworkbuddy.data.model.ChatEvent
import space.jtcao.vpworkbuddy.data.model.ChatMessage
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Custom SSE (Server-Sent Events) client for the VisePanda chat API.
 *
 * Android has no built-in SSE support, so we use OkHttp's streaming response
 * and parse the SSE format manually.
 *
 * SSE format:
 *   event: token
 *   data: "some text"
 *
 *   event: image
 *   data: {"key":"..","url":"..","label":".."}
 *
 *   event: done
 *   data:
 *
 */
class SseClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.SSE_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Stream chat responses from the VisePanda API.
     *
     * @param messages The conversation history (previous messages)
     * @param city Optional city context (null = general chat)
     * @return Flow of [ChatEvent] — tokens, images, FAQs, done signal
     */
    fun streamChat(
        messages: List<ChatMessage>,
        city: String?
    ): Flow<ChatEvent> = callbackFlow {
        val bodyJson = buildJsonBody(messages, city)
        val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/chat")
            .post(requestBody)
            .header("User-Agent", "VisePanda-Android/1.0")
            .build()

        var currentCall: Call? = null

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                currentCall = call
                val source = response.body?.source() ?: run {
                    trySend(ChatEvent.Error("Empty response body"))
                    channel.close()
                    return
                }

                var currentEvent = "message"

                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        when {
                            line.startsWith("event: ") -> {
                                currentEvent = line.removePrefix("event: ").trim()
                            }
                            line.startsWith("data: ") -> {
                                val data = line.removePrefix("data: ").trim()
                                val event = parseEvent(currentEvent, data)
                                if (event != null) {
                                    trySend(event)
                                }
                            }
                            line.isEmpty() -> {
                                // Empty line = event boundary, reset
                                currentEvent = "message"
                            }
                        }
                    }
                } catch (e: Exception) {
                    trySend(ChatEvent.Error(e.message ?: "Stream read error"))
                } finally {
                    trySend(ChatEvent.Done)
                    channel.close()
                    response.closeQuietly()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                trySend(ChatEvent.Error(e.message ?: "Network error"))
                channel.close()
            }
        })

        awaitClose {
            currentCall?.cancel()
        }
    }

    /**
     * Non-streaming fallback — fires the request and returns the full response text.
     * Used as a simpler alternative when SSE is not required.
     */
    fun chatSync(messages: List<ChatMessage>, city: String?): String {
        val bodyJson = buildJsonBody(messages, city)
        val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/chat")
            .post(requestBody)
            .header("User-Agent", "VisePanda-Android/1.0")
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    // ── Private helpers ──

    private fun buildJsonBody(messages: List<ChatMessage>, city: String?): JsonObject {
        return buildJsonObject {
            put("messages", buildJsonArray {
                messages.forEach { msg ->
                    add(buildJsonObject {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
            })
            if (city != null) {
                put("city", city)
            }
        }
    }

    private fun parseEvent(eventType: String, data: String): ChatEvent? {
        if (data.isEmpty()) return null

        return when (eventType) {
            "token" -> ChatEvent.Token(data.removeSurrounding("\""))
            "split" -> ChatEvent.Split(true)
            "image" -> try {
                val obj = json.parseToJsonElement(data).jsonObject
                ChatEvent.Image(
                    key = obj["key"]?.jsonPrimitive?.content ?: "",
                    url = obj["url"]?.jsonPrimitive?.content ?: "",
                    label = obj["label"]?.jsonPrimitive?.content ?: ""
                )
            } catch (e: Exception) {
                null
            }
            "faq" -> try {
                val obj = json.parseToJsonElement(data).jsonObject
                ChatEvent.Faq(
                    id = obj["id"]?.jsonPrimitive?.content ?: "",
                    title = obj["title"]?.jsonPrimitive?.content ?: "",
                    icon = obj["icon"]?.jsonPrimitive?.content ?: ""
                )
            } catch (e: Exception) {
                null
            }
            "done" -> ChatEvent.Done
            "error" -> ChatEvent.Error(
                try {
                    json.parseToJsonElement(data).jsonObject["message"]?.jsonPrimitive?.content ?: data
                } catch (e: Exception) {
                    data
                }
            )
            else -> null
        }
    }
}
