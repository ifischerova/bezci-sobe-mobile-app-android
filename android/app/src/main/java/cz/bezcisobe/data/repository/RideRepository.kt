package cz.bezcisobe.data.repository

import cz.bezcisobe.data.mapper.toDomain
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.CreateRideRequestDto
import javax.inject.Inject

/**
 * Network-only repository for carpooling rides. Rides change often, so there is no Room cache.
 */
class RideRepository @Inject constructor(
    private val api: ApiService,
) {
    suspend fun getRides(raceId: Long): List<Ride> =
        api.getRides(raceId).map { it.toDomain() }

    suspend fun createRide(request: CreateRideRequestDto): Ride =
        api.createRide(request).toDomain()

    suspend fun deleteRide(id: String) {
        api.deleteRide(id)
    }

    suspend fun acceptRide(id: String): Ride = api.acceptRide(id).toDomain()

    suspend fun cancelRide(id: String): Ride = api.cancelRide(id).toDomain()
}
