package com.visepanda.network.sse

import com.visepanda.common.AppError
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class SseEvent {
    data class Token(val text: String) : SseEvent()
    data class Split(val text: String) : SseEvent()
    data class Image(val url: String) : SseEvent()
    data class Faq(val data: String) : SseEvent()
    data object Done : SseEvent()
    data class Error(val message: String) : SseEvent()
}

class SseClient {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun connect(
        url: String,
        requestBody: String
    ): Flow<SseEvent> = callbackFlow {
        val body = requestBody.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Accept", "text/event-stream")
            .build()

        val call = com.visepanda.network.HttpClientProvider.client
            .newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                trySend(SseEvent.Error(e.message ?: "Network error"))
                close()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    trySend(SseEvent.Error("HTTP ${response.code}: ${response.message}"))
                    close()
                    return
                }

                try {
                    val reader = BufferedReader(
                        InputStreamReader(response.body?.byteStream())
                    )
                    var line: String?
                    var currentEvent = ""
                    var currentData = StringBuilder()

                    while (reader.readLine().also { line = it } != null) {
                        val l = line ?: continue

                        when {
                            l.startsWith("event:") -> {
                                currentEvent = l.removePrefix("event:").trim()
                            }
                            l.startsWith("data:") -> {
                                currentData.append(l.removePrefix("data:").trim())
                            }
                            l.isEmpty() && currentData.isNotEmpty() -> {
                                val data = currentData.toString()
                                currentData = StringBuilder()

                                when (currentEvent) {
                                    "message" -> {
                                        // Parse JSON payload with type field
                                        try {
                                            val payload = com.google.gson.JsonParser.parseString(data).asJsonObject
                                            val type = payload.get("type")?.asString ?: ""
                                            val content = payload.get("content")?.asString ?: data

                                            when (type) {
                                                "token" -> trySend(SseEvent.Token(content))
                                                "split" -> trySend(SseEvent.Split(content))
                                                "image" -> trySend(SseEvent.Image(content))
                                                "faq" -> trySend(SseEvent.Faq(content))
                                                "done" -> {
                                                    trySend(SseEvent.Done)
                                                    close()
                                                    return
                                                }
                                                else -> trySend(SseEvent.Token(content))
                                            }
                                        } catch (e: Exception) {
                                            trySend(SseEvent.Token(data))
                                        }
                                    }
                                    "done" -> {
                                        trySend(SseEvent.Done)
                                        close()
                                        return
                                    }
                                    "error" -> {
                                        trySend(SseEvent.Error(data))
                                        close()
                                        return
                                    }
                                }
                            }
                        }
                    }

                    // Stream ended without done event
                    trySend(SseEvent.Done)
                    close()
                } catch (e: Exception) {
                    trySend(SseEvent.Error(e.message ?: "Parse error"))
                    close()
                }
            }
        })

        awaitClose {
            if (!call.isCanceled()) call.cancel()
        }
    }
}
