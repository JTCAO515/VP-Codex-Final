package space.jtcao.visepanda.data.trip

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.jtcao.visepanda.data.model.Trip
import space.jtcao.visepanda.data.repository.TripRepository
import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.repository.TripAssetRepository

class TripAssetRepositoryImpl(
    private val getTrips: () -> Flow<List<Trip>>,
    private val saveTrip: suspend (Trip) -> Unit,
    private val deleteTrip: suspend (String) -> Unit
) : TripAssetRepository {

    constructor(legacyRepository: TripRepository) : this(
        getTrips = legacyRepository::getAllTrips,
        saveTrip = legacyRepository::saveTrip,
        deleteTrip = legacyRepository::deleteTrip
    )

    override suspend fun save(asset: TripAsset) {
        saveTrip(asset.toLegacyTrip())
    }

    override fun observe(): Flow<List<TripAsset>> =
        getTrips().map { trips -> trips.map(Trip::toTripAsset) }

    override suspend fun delete(id: String) {
        deleteTrip(id)
    }
}

fun Trip.toTripAsset(): TripAsset =
    TripAsset(
        id = id,
        title = title,
        cityId = city,
        days = days,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun TripAsset.toLegacyTrip(): Trip =
    Trip(
        id = id,
        title = title,
        city = cityId,
        days = days,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
