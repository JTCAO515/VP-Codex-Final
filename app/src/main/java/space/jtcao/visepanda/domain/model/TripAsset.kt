package space.jtcao.visepanda.domain.model

data class TripAsset(
    val id: String = "",
    val title: String = "",
    val cityId: String = "",
    val days: Int = 0,
    val content: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
