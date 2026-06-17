package space.jtcao.visepanda.domain.repository

import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

interface DestinationRepository {
    suspend fun getFeaturedDestinations(): List<DestinationSummary>
    suspend fun getExploreDestinations(): List<DestinationSummary>
    suspend fun getDestinationDetail(cityId: String): DestinationDetail
}
