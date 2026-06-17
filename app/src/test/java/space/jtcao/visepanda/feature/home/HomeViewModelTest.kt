package space.jtcao.visepanda.feature.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository
import space.jtcao.visepanda.domain.usecase.GetFeaturedDestinationsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load should expose success state with featured destinations`() = runTest {
        val repo = object : DestinationRepository {
            override suspend fun getFeaturedDestinations() = listOf(
                DestinationSummary(
                    "beijing",
                    "Beijing",
                    "Ancient Capital",
                    "Imperial culture",
                    39.9042,
                    116.4074
                )
            )

            override suspend fun getExploreDestinations(): List<DestinationSummary> = emptyList()

            override suspend fun getDestinationDetail(cityId: String) =
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
        }

        val viewModel = HomeViewModel(GetFeaturedDestinationsUseCase(repo))

        viewModel.load()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Success)
    }
}
