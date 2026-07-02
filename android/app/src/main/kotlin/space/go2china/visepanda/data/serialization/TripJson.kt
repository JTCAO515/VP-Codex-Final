package space.go2china.visepanda.data.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.TripState

object TripJson {
    val gson: Gson = GsonBuilder().create()

    fun encodeTrip(trip: TripState): String = gson.toJson(trip)

    fun decodeTrip(json: String): TripState = gson.fromJson(json, TripState::class.java)

    fun encodeMessages(messages: List<ButlerChatMessage>): String = gson.toJson(messages)

    fun decodeMessages(json: String): List<ButlerChatMessage> {
        val array = gson.fromJson(json, Array<ButlerChatMessage>::class.java)
        return array?.toList().orEmpty()
    }
}
