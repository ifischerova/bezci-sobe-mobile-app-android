package cz.bezcisobe.data.mapper

import cz.bezcisobe.data.local.RaceEntity
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.repository.Race

fun RaceDto.toEntity() = RaceEntity(
    id = id, name = name, place = place, date = date, startTime = startTime, web = web,
    trackLength = trackLength?.name, trackType = trackType?.name, isPast = isPast,
)

fun RaceEntity.toDomain() = Race(
    id = id, name = name, place = place, date = date, startTime = startTime, web = web,
    trackLength = trackLength, trackType = trackType, isPast = isPast,
)
