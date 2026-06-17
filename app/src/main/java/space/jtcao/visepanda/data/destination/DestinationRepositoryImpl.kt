package space.jtcao.visepanda.data.destination

import space.jtcao.visepanda.core.common.AppMode
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class DestinationRepositoryImpl(
    private val appMode: AppMode,
    private val remote: DestinationDataSource,
    private val mock: DestinationDataSource
) : DestinationRepository {

    private val activeDataSource: DestinationDataSource
        get() = if (appMode == AppMode.MOCK) mock else remote

    override suspend fun getFeaturedDestinations(): List<DestinationSummary> =
        activeDataSource.getFeatured()

    override suspend fun getExploreDestinations(): List<DestinationSummary> =
        activeDataSource.getExplore()

    override suspend fun getDestinationDetail(cityId: String): DestinationDetail =
        activeDataSource.getDetail(cityId)
}
