package cz.bezcisobe.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RideDto(
    val id: String,
    val raceId: String,
    val racePast: Boolean = false,
    val userId: String,
    val userUsername: String,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val type: String,
    val from: String,
    val to: String? = null,
    val car: String? = null,
    val availableSeats: Int = 0,
    val occupiedSeats: Int = 0,
    val passengers: List<String> = emptyList(),
    val notes: String? = null,
    val createdAt: String,
)

@Serializable
data class CreateRideRequestDto(
    val raceId: Long,
    val type: String,
    val from: String,
    val to: String? = null,
    val car: String? = null,
    val availableSeats: Int,
    val notes: String? = null,
)
