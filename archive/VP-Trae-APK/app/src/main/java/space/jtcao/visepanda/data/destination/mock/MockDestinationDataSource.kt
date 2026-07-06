package space.jtcao.visepanda.data.destination.mock

import space.jtcao.visepanda.data.destination.DestinationDataSource
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

class MockDestinationDataSource : DestinationDataSource {

    override suspend fun getFeatured(): List<DestinationSummary> = listOf(
        DestinationSummary(
            id = "shanghai",
            name = "Shanghai",
            tagline = "Magic City",
            vibe = "Editorial luxury",
            lat = 31.2304,
            lng = 121.4737
        ),
        DestinationSummary(
            id = "beijing",
            name = "Beijing",
            tagline = "Ancient Capital",
            vibe = "Imperial culture",
            lat = 39.9042,
            lng = 116.4074
        ),
        DestinationSummary(
            id = "hangzhou",
            name = "Hangzhou",
            tagline = "West Lake",
            vibe = "Poetic water city",
            lat = 30.2741,
            lng = 120.1551
        )
    )

    override suspend fun getExplore(): List<DestinationSummary> = getFeatured() + listOf(
        DestinationSummary(
            id = "chengdu",
            name = "Chengdu",
            tagline = "Panda Capital",
            vibe = "Tea house ease",
            lat = 30.5728,
            lng = 104.0668
        )
    )

    override suspend fun getDetail(cityId: String): DestinationDetail {
        val summary = getExplore().firstOrNull { it.id == cityId }
            ?: DestinationSummary(
                id = cityId,
                name = cityId.replaceFirstChar { it.uppercase() },
                tagline = "Curated destination",
                vibe = "Elegant discovery",
                lat = 0.0,
                lng = 0.0
            )

        return DestinationDetail(
            id = summary.id,
            name = summary.name,
            headline = "${summary.name} for elegant first-time China travel.",
            bestDays = "3-4 days",
            budget = "$$$",
            highlights = listOf("Iconic skyline", "Historic quarters", "Night strolls"),
            foods = listOf("Signature noodles", "Local brunch", "Tea desserts"),
            tips = listOf("Carry Alipay", "Wear comfortable shoes", "Start early for popular sites")
        )
    }
}
