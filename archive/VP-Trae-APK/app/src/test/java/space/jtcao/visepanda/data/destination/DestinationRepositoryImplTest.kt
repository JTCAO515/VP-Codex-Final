package space.jtcao.visepanda.data.destination

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.core.common.AppMode
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary

class DestinationRepositoryImplTest {

    @Test
    fun `mock mode should read from mock data source`() = runBlocking {
        val repo = DestinationRepositoryImpl(
            appMode = AppMode.MOCK,
            remote = object : DestinationDataSource {
                override suspend fun getFeatured() = error("remote should not be used")
                override suspend fun getExplore() = error("remote should not be used")
                override suspend fun getDetail(cityId: String) = error("remote should not be used")
            },
            mock = object : DestinationDataSource {
                override suspend fun getFeatured() = listOf(
                    DestinationSummary(
                        "hangzhou",
                        "Hangzhou",
                        "West Lake",
                        "Poetic water city",
                        30.2741,
                        120.1551
                    )
                )

                override suspend fun getExplore() = emptyList<DestinationSummary>()

                override suspend fun getDetail(cityId: String) =
                    DestinationDetail(
                        cityId,
                        "Hangzhou",
                        "Poetic city",
                        "3 days",
                        "$$$",
                        emptyList(),
                        emptyList(),
                        emptyList()
                    )
            }
        )

        val result = repo.getFeaturedDestinations()

        assertEquals("hangzhou", result.first().id)
    }

    @Test
    fun `real mode should read from remote data source`() = runBlocking {
        val repo = DestinationRepositoryImpl(
            appMode = AppMode.REAL,
            remote = object : DestinationDataSource {
                override suspend fun getFeatured() = listOf(
                    DestinationSummary(
                        "beijing",
                        "Beijing",
                        "Ancient Capital",
                        "Imperial culture",
                        39.9042,
                        116.4074
                    )
                )

                override suspend fun getExplore() = emptyList<DestinationSummary>()

                override suspend fun getDetail(cityId: String) =
                    DestinationDetail(
                        cityId,
                        "Beijing",
                        "Ancient Capital",
                        "4 days",
                        "$$$",
                        emptyList(),
                        emptyList(),
                        emptyList()
                    )
            },
            mock = object : DestinationDataSource {
                override suspend fun getFeatured() = error("mock should not be used")
                override suspend fun getExplore() = error("mock should not be used")
                override suspend fun getDetail(cityId: String) = error("mock should not be used")
            }
        )

        val result = repo.getFeaturedDestinations()

        assertEquals("beijing", result.first().id)
    }
}
