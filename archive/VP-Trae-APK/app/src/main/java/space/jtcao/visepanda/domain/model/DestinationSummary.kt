package space.jtcao.visepanda.domain.model

data class DestinationSummary(
    val id: String,
    val name: String,
    val tagline: String,
    val vibe: String,
    val lat: Double,
    val lng: Double
)
