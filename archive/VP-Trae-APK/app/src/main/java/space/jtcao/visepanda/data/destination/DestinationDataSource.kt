package space.jtcao.visepanda.data.destination

import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

interface DestinationDataSource {
    suspend fun getFeatured(): List<DestinationSummary>
    suspend fun getExplore(): List<DestinationSummary>
    suspend fun getDetail(cityId: String): DestinationDetail
}
