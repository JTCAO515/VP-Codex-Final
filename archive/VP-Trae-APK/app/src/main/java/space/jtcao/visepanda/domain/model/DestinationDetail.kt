package space.jtcao.visepanda.domain.model

data class DestinationDetail(
    val id: String,
    val name: String,
    val headline: String,
    val bestDays: String,
    val budget: String,
    val highlights: List<String>,
    val foods: List<String>,
    val tips: List<String>
)
