package space.go2china.visepanda.data.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.TripBlock
import space.go2china.visepanda.data.model.TripDay
import space.go2china.visepanda.data.model.TripState

object TripJson {
    val gson: Gson = GsonBuilder().create()

    fun encodeTrip(trip: TripState): String = gson.toJson(trip)

    fun decodeTrip(json: String): TripState = gson.fromJson(json, TripState::class.java).normalizeNulls()

    fun encodeMessages(messages: List<ButlerChatMessage>): String = gson.toJson(messages)

    fun decodeMessages(json: String): List<ButlerChatMessage> {
        val array = gson.fromJson(json, Array<ButlerChatMessage>::class.java)
        return array?.toList().orEmpty()
    }

    /**
     * Gson's unsafe-allocation deserialization bypasses Kotlin constructor
     * defaults, so a persisted blob from before a list field existed can
     * leave a "non-null" `List` property actually null at the JVM level.
     * Reading it is harmless, but passing it into `.copy()` is not: the
     * compiler-generated `copy()` re-validates every constructor parameter
     * as non-null and throws. Rebuild each object here, right at the
     * decode boundary, so nulls never reach the rest of the app. See
     * DESIGN.md ADR-118.
     */
    @Suppress("SENSELESS_COMPARISON")
    private fun TripState.normalizeNulls(): TripState = TripState(
        summary = summary,
        days = (days ?: emptyList()).map { it.normalizeNulls() },
        alerts = alerts ?: emptyList(),
        lastUpdatedReason = lastUpdatedReason,
    )

    @Suppress("SENSELESS_COMPARISON")
    private fun TripDay.normalizeNulls(): TripDay = TripDay(
        day = day,
        city = city,
        pace = pace,
        blocks = (blocks ?: emptyList()).map { it.normalizeNulls() },
        food = food ?: emptyList(),
        stay = stay,
        transport = transport,
        note = note,
        status = status,
    )

    @Suppress("SENSELESS_COMPARISON")
    private fun TripBlock.normalizeNulls(): TripBlock = TripBlock(
        time = time,
        title = title,
        description = description,
        highlights = highlights ?: emptyList(),
        photoUrl = photoUrl,
        address = address,
        chineseAddress = chineseAddress,
        phone = phone,
        openingHours = openingHours,
        mapUrl = mapUrl,
        bookingUrl = bookingUrl,
        bookingCandidates = bookingCandidates ?: emptyList(),
        sourceLabel = sourceLabel,
        coordinates = coordinates,
    )
}
