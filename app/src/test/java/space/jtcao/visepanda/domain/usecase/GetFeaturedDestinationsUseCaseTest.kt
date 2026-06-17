package space.jtcao.visepanda.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetFeaturedDestinationsUseCaseTest {

    @Test
    fun `invoke should return top featured destinations from repository`() {
        val repo = object : DestinationRepository {
            override suspend fun getFeaturedDestinations(): List<DestinationSummary> {
                return listOf(
                    DestinationSummary("shanghai", "Shanghai", "Magic City", "Editorial luxury", 31.2304, 121.4737),
                    DestinationSummary("beijing", "Beijing", "Ancient Capital", "Imperial culture", 39.9042, 116.4074)
                )
            }

            override suspend fun getExploreDestinations(): List<DestinationSummary> = emptyList()

            override suspend fun getDestinationDetail(cityId: String) =
                throw IllegalStateException("not needed")
        }

        val useCase = GetFeaturedDestinationsUseCase(repo)

        val result = kotlinx.coroutines.runBlocking { useCase() }

        assertEquals(2, result.size)
        assertEquals("shanghai", result.first().id)
    }
}
