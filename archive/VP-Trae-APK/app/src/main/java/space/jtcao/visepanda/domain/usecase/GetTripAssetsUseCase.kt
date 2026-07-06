package space.jtcao.visepanda.domain.usecase

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.repository.TripAssetRepository

class GetTripAssetsUseCase(
    private val repository: TripAssetRepository
) {
    operator fun invoke(): Flow<List<TripAsset>> = repository.observe()
}
