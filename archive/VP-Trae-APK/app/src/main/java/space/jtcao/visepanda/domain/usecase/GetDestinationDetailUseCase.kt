package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.DestinationDetail
import space.jtcao.visepanda.domain.repository.DestinationRepository

class GetDestinationDetailUseCase(
    private val repository: DestinationRepository
) {
    suspend operator fun invoke(cityId: String): DestinationDetail {
        return repository.getDestinationDetail(cityId)
    }
}
