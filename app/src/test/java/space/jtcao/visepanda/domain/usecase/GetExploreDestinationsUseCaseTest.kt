package space.jtcao.visepanda.domain.usecase

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetExploreDestinationsUseCaseTest {

    @Test
    fun `invoke should return explore destinations`() = runBlocking {
        val repo = object : DestinationRepository {
            override suspend fun getFeaturedDestinations(): List<DestinationSummary> = emptyList()

            override suspend fun getExploreDestinations(): List<DestinationSummary> = listOf(
                DestinationSummary("chengdu", "Chengdu", "Panda capital", "Tea house ease", 30.5728, 104.0668)
            )

            override suspend fun getDestinationDetail(cityId: String) =
                DestinationDetail(cityId, "Chengdu", "Panda capital", "3 days", "$$", emptyList(), emptyList(), emptyList())
        }

        val result = GetExploreDestinationsUseCase(repo).invoke()

        assertEquals("chengdu", result.first().id)
    }
}
