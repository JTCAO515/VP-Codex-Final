package com.visepanda.hermes.data

import android.content.Context
import com.visepanda.network.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TripItem(
    val id: String,
    val title: String,
    val city: String,
    val days: String,
    val preview: String
)

class TripRepository(private val context: Context) {

    private val authRepo = AuthRepository(context)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    private fun getToken(): String {
        return authRepo.getToken() ?: ""
    }

    /**
     * Fetch user's trips from backend.
     */
    suspend fun getTrips(): Result<Pair<List<TripItem>, List<TripItem>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url("${ApiConfig.BASE_URL}${ApiConfig.TRIPS}")
                    .header("Authorization", "Bearer ${getToken()}")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: throw Exception("Empty response")
                if (!response.isSuccessful) {
                    val err = JSONObject(body).optString("error", "Failed to fetch trips")
                    throw Exception(err)
                }

                val json = JSONObject(body)
                val trips = json.optJSONObject("trips") ?: JSONObject()

                val recent = parseTripArray(trips.optJSONArray("recent"))
                val saved = parseTripArray(trips.optJSONArray("saved"))

                Pair(recent, saved)
            }
        }

    /**
     * Create a new trip on the backend.
     */
    suspend fun createTrip(
        title: String,
        city: String,
        days: String,
        preview: String,
        isSaved: Boolean = false
    ): Result<TripItem> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject().apply {
                put("title", title)
                put("city", city)
                put("days", days)
                put("preview", preview)
                put("is_saved", isSaved)
            }.toString().toRequestBody(JSON_MEDIA)

            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}${ApiConfig.TRIPS}")
                .header("Authorization", "Bearer ${getToken()}")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: throw Exception("Empty response")
            if (!response.isSuccessful) {
                val err = JSONObject(respBody).optString("error", "Failed to create trip")
                throw Exception(err)
            }

            val json = JSONObject(respBody)
            val trip = json.optJSONObject("trip") ?: throw Exception("No trip in response")
            TripItem(
                id = trip.optString("id", ""),
                title = trip.optString("title", ""),
                city = trip.optString("city", ""),
                days = trip.optString("days", ""),
                preview = trip.optString("preview", "")
            )
        }
    }

    /**
     * Delete a trip from the backend.
     */
    suspend fun deleteTrip(tripId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}${ApiConfig.TRIPS}/$tripId")
                .header("Authorization", "Bearer ${getToken()}")
                .delete()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val err = JSONObject(body).optString("error", "Failed to delete trip")
                throw Exception(err)
            }
        }
    }

    private fun parseTripArray(arr: JSONArray?): List<TripItem> {
        if (arr == null || arr.length() == 0) return emptyList()
        return (0 until arr.length()).map { i ->
            val t = arr.getJSONObject(i)
            TripItem(
                id = t.optString("id", ""),
                title = t.optString("title", ""),
                city = t.optString("city", ""),
                days = t.optString("days", ""),
                preview = t.optString("preview", "")
            )
        }
    }
}
