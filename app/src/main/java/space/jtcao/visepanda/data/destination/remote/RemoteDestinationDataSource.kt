package space.jtcao.visepanda.data.destination.remote

import space.jtcao.visepanda.data.destination.DestinationDataSource
import space.jtcao.visepanda.data.repository.CityRepository
import space.jtcao.visepanda.data.repository.MapRepository
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

class RemoteDestinationDataSource(
    private val cityRepository: CityRepository = CityRepository(),
    private val mapRepository: MapRepository = MapRepository()
) : DestinationDataSource {

    override suspend fun getFeatured(): List<DestinationSummary> = getExplore().take(3)

    override suspend fun getExplore(): List<DestinationSummary> {
        val markersBySlug = mapRepository.getMarkers().associateBy { it.name.lowercase() }

        return cityRepository.getCities().map { (slug, city) ->
            val marker = markersBySlug[slug.lowercase()]

            DestinationSummary(
                id = slug,
                name = slug.toDisplayName(),
                tagline = city.nameCn.ifBlank { city.province },
                vibe = city.vibe.ifBlank { city.bestSeason },
                lat = marker?.lat ?: 0.0,
                lng = marker?.lng ?: 0.0
            )
        }
    }

    override suspend fun getDetail(cityId: String): DestinationDetail {
        val cityDetail = cityRepository.getCityDetail(cityId)

        return DestinationDetail(
            id = cityId,
            name = cityId.toDisplayName(),
            headline = cityDetail.vibe.ifBlank { cityDetail.nameCn },
            bestDays = cityDetail.days.ifBlank { cityDetail.bestSeason },
            budget = cityDetail.estimate.midDaily.ifBlank { cityDetail.budgetTip },
            highlights = cityDetail.highlights,
            foods = cityDetail.food.map { item -> item.nameCn.ifBlank { item.nameEn } },
            tips = cityDetail.tips.map { tip -> tip.tip.ifBlank { tip.en } }
        )
    }
}

private fun String.toDisplayName(): String =
    split("-", "_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { first ->
                if (first.isLowerCase()) {
                    first.titlecase()
                } else {
                    first.toString()
                }
            }
        }
