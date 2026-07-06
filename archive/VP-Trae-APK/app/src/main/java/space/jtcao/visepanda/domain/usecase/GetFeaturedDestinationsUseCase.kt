package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.DestinationSummary
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetFeaturedDestinationsUseCase(
    private val repository: DestinationRepository
) {
    suspend operator fun invoke(): List<DestinationSummary> {
        return repository.getFeaturedDestinations()
    }
}
