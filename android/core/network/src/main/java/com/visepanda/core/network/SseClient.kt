package com.visepanda.core.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject

class SseClient(private val okHttpClient: OkHttpClient = ApiClient.okHttpClient) {

    fun connect(url: String, body: String): Flow<SseEvent> = callbackFlow {
        val request = Request.Builder()
            .url(url)
            .post(okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), body))
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val json = JSONObject(data)
                    val eventType = json.optString("type", "unknown")
                    val content = json.opt("content")
                    when (eventType) {
                        "token" -> trySend(SseEvent.Token(content?.toString() ?: ""))
                        "itinerary" -> trySend(SseEvent.Itinerary(content))
                        "image" -> trySend(SseEvent.Image(json.optString("url"), json.optString("alt")))
                        "faq" -> trySend(SseEvent.Faq(content))
                        "done" -> trySend(SseEvent.Done)
                    }
                } catch (e: Exception) {
                    trySend(SseEvent.Error(e.message ?: "Parse error"))
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                trySend(SseEvent.Error(t?.message ?: "Connection failed"))
                close()
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val factory = EventSources.createFactory(okHttpClient)
        val eventSource = factory.newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }
}

sealed class SseEvent {
    data class Token(val text: String) : SseEvent()
    data class Itinerary(val data: Any?) : SseEvent()
    data class Image(val url: String, val alt: String) : SseEvent()
    data class Faq(val data: Any?) : SseEvent()
    data object Done : SseEvent()
    data class Error(val message: String) : SseEvent()
}
