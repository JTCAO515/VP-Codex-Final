package com.visepanda.network

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.visepanda.network.sse.SseClient
import com.visepanda.network.sse.SseEvent
import kotlinx.coroutines.flow.Flow
import okhttp3.Request

class ApiService {

    private val client = HttpClientProvider.client
    private val gson = Gson()
    private val sseClient = SseClient()

    suspend fun fetchCities(): Result<String> = runCatching {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}${ApiConfig.CITIES}")
            .get()
            .build()

        val response = client.newCall(request).execute()
        response.body?.string() ?: throw Exception("Empty response body")
    }

    suspend fun fetchMap(): Result<String> = runCatching {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}${ApiConfig.MAP}")
            .get()
            .build()

        val response = client.newCall(request).execute()
        response.body?.string() ?: throw Exception("Empty response body")
    }

    suspend fun fetchTools(): Result<String> = runCatching {
        val request = Request.Builder()
            .url("${ApiConfig.BASE_URL}${ApiConfig.TOOLS}")
            .get()
            .build()

        val response = client.newCall(request).execute()
        response.body?.string() ?: throw Exception("Empty response body")
    }

    fun chat(messages: List<Map<String, String>>): Flow<SseEvent> {
        val body = gson.toJson(mapOf("messages" to messages))
        return sseClient.connect("${ApiConfig.BASE_URL}${ApiConfig.CHAT}", body)
    }
}
