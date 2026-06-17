package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.repository.TripAssetRepository

class SaveTripAssetUseCase(
    private val repository: TripAssetRepository
) {
    suspend operator fun invoke(asset: TripAsset) = repository.save(asset)
}
