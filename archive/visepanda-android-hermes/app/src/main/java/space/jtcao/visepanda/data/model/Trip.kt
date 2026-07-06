package space.jtcao.visepanda.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A saved trip / itinerary.
 * Stored locally via DataStore — serialized as JSON.
 */
@Serializable
data class Trip(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("city") val city: String = "",
    @SerialName("days") val days: Int = 0,
    @SerialName("content") val content: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)
