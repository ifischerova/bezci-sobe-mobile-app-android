package cz.bezcisobe.data.mapper

import cz.bezcisobe.data.remote.dto.RideDto
import cz.bezcisobe.data.repository.Ride
import cz.bezcisobe.data.repository.RideType

fun RideDto.toDomain() = Ride(
    id = id,
    raceId = raceId,
    userId = userId,
    userUsername = userUsername,
    userFirstName = userFirstName,
    userLastName = userLastName,
    type = if (type.equals("OFFER", ignoreCase = true)) RideType.OFFER else RideType.REQUEST,
    from = from,
    to = to,
    car = car,
    availableSeats = availableSeats,
    occupiedSeats = occupiedSeats,
    passengers = passengers,
    notes = notes,
)
