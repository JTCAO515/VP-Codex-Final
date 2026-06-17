package space.jtcao.visepanda.domain.repository

import kotlinx.coroutines.flow.Flow
import space.jtcao.visepanda.domain.model.TripAsset

interface TripAssetRepository {
    suspend fun save(asset: TripAsset)
    fun observe(): Flow<List<TripAsset>>
    suspend fun delete(id: String)
}
