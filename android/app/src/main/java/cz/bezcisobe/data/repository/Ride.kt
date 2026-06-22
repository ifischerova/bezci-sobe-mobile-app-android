package cz.bezcisobe.data.repository

enum class RideType { OFFER, REQUEST }

data class Ride(
    val id: String,
    val raceId: String,
    val userId: String,
    val userUsername: String,
    val userFirstName: String?,
    val userLastName: String?,
    val type: RideType,
    val from: String,
    val to: String?,
    val car: String?,
    val availableSeats: Int,
    val occupiedSeats: Int,
    val passengers: List<String>,
    val notes: String?,
) {
    val displayName: String
        get() {
            val full = listOfNotNull(userFirstName, userLastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            return full.ifBlank { userUsername }
        }

    val freeSeats: Int get() = (availableSeats - occupiedSeats).coerceAtLeast(0)
    val isFull: Boolean get() = type == RideType.OFFER && freeSeats <= 0
}
