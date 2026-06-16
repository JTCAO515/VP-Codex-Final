     1|package space.jtcao.visepanda.data.model
     2|
     3|import kotlinx.serialization.SerialName
     4|import kotlinx.serialization.Serializable
     5|
     6|/**
     7| * A saved trip / itinerary.
     8| * Stored locally via DataStore — serialized as JSON.
     9| */
    10|@Serializable
    11|data class Trip(
    12|    @SerialName("id") val id: String = "",
    13|    @SerialName("title") val title: String = "",
    14|    @SerialName("city") val city: String = "",
    15|    @SerialName("days") val days: Int = 0,
    16|    @SerialName("content") val content: String = "",
    17|    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    18|    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
    19|)
    20|
    21|/**
    22| * Map markers — from GET /api/map
    23| */
    24|@Serializable
    27|    @SerialName("name_cn") val nameCn: String = "",
    28|    @SerialName("lat") val lat: Double = 0.0,
    29|    @SerialName("lng") val lng: Double = 0.0,
    30|    @SerialName("vibe") val vibe: String = "",
    31|    @SerialName("days") val days: String = ""
    32|)
    33|
    34|@Serializable
    37|)
    38|
    39|/**
    40| * App configuration — from GET /api/config
    41| */
    42|@Serializable
    45|    @SerialName("map_center") val mapCenter: MapCenter? = null
    46|)
    47|
    48|@Serializable
    51|    @SerialName("lng") val lng: Double = 104.19
    52|)
    53|