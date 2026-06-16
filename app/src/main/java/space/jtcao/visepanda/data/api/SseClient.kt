package space.jtcao.visepanda.data.api

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import space.jtcao.visepanda.data.model.ChatEvent
import space.jtcao.visepanda.data.model.ChatFaq
import space.jtcao.visepanda.data.model.ChatImage
import space.jtcao.visepanda.data.model.ChatMessage
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * SSE Chat client for VisePanda backend.
 *
 * **Actual protocol (2026)**
 * The backend always emits `event: message` with a JSON payload that
 * encodes the payload type inside:
 *   data: {"token": "text"}
 *   data: {"split": true}
 *   data: {"image": {"key":"..","url":"..","label":".."}}
 *   data: {"faq": {"id":"..","title":"..","icon":".."}}
 *   data: {"error": "msg"}
 *   data: {"done": true}
 *
 * Outgoing messages are a JSON array of {role, content} objects.
 */
class SseClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun streamChat(messages: List<ChatMessage>, city: String? = null): Flow<ChatEvent> = callbackFlow {
        // Build request body from messages
        val messagesJson = buildString {
            append("[")
            messages.forEachIndexed { i, msg ->
                if (i > 0) append(",")
                append("""{"role":"${msg.role}","content":"${escapeJson(msg.content)}"}""")
            }
            append("]")
        }

        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/chat")
            .post(messagesJson.toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                trySend(ChatEvent.Error(e.message ?: "Connection failed"))
                close()
            }

            override fun onResponse(call: Call, response: Response) {
                val source = response.body?.source() ?: run {
                    trySend(ChatEvent.Error("Empty response body"))
                    close()
                    return
                }

                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("data:")) {
                            val data = line.removePrefix("data:").trim()
                            if (data.isEmpty()) continue
                            parseData(data)
                        }
                    }
                } catch (e: Exception) {
                    trySend(ChatEvent.Error(e.message ?: "Parse error"))
                } finally {
                    close()
                }
            }

            private fun parseData(data: String) {
                try {
                    val element = json.parseToJsonElement(data).jsonObject

                    when {
                        element.containsKey("token") -> {
                            val text = element["token"]?.jsonPrimitive?.content ?: ""
                            trySend(ChatEvent.Token(text))
                        }
                        element.containsKey("split") -> {
                            trySend(ChatEvent.Split())
                        }
                        element.containsKey("image") -> {
                            val img = element["image"]?.jsonObject
                            if (img != null) {
                                trySend(ChatEvent.Image(
                                    key = img["key"]?.jsonPrimitive?.content ?: "",
                                    url = img["url"]?.jsonPrimitive?.content ?: "",
                                    label = img["label"]?.jsonPrimitive?.content ?: ""
                                ))
                            }
                        }
                        element.containsKey("faq") -> {
                            val faq = element["faq"]?.jsonObject
                            if (faq != null) {
                                trySend(ChatEvent.Faq(
                                    id = faq["id"]?.jsonPrimitive?.content ?: "",
                                    title = faq["title"]?.jsonPrimitive?.content ?: "",
                                    icon = faq["icon"]?.jsonPrimitive?.content ?: ""
                                ))
                            }
                        }
                        element.containsKey("error") -> {
                            val msg = element["error"]?.jsonPrimitive?.content ?: data
                            trySend(ChatEvent.Error(msg))
                        }
                        element.containsKey("done") -> {
                            trySend(ChatEvent.Done)
                        }
                    }
                } catch (_: Exception) {
                    // Not valid JSON — ignore
                }
            }
        })

        awaitClose { call.cancel() }
    }

    /** Synchronous fallback for simple queries. */
    suspend fun chatSync(messages: List<ChatMessage>, city: String? = null): String {
        val messagesJson = buildString {
            append("[")
            messages.forEachIndexed { i, msg ->
                if (i > 0) append(",")
                append("""{"role":"${msg.role}","content":"${escapeJson(msg.content)}"}""")
            }
            append("]")
        }

        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}/api/chat")
            .post(messagesJson.toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return ""

        // Extract tokens from SSE stream
        val sb = StringBuilder()
        body.lines().forEach { line ->
            if (line.startsWith("data:")) {
                val data = line.removePrefix("data:").trim()
                try {
                    val element = json.parseToJsonElement(data).jsonObject
                    val token = element["token"]?.jsonPrimitive?.content
                    if (token != null) sb.append(token)
                } catch (_: Exception) {}
            }
        }
        return sb.toString()
    }

    private fun escapeJson(s: String): String = buildString {
        for (c in s) {
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }
}
