package cz.bezcisobe.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RaceDto(
    val id: String,
    val name: String,
    val place: String,
    val date: String,
    val startTime: String? = null,
    val web: String? = null,
    val trackLength: NamedRefDto? = null,
    val trackType: NamedRefDto? = null,
    val certifications: List<NamedRefDto> = emptyList(),
    val raceCalendarId: String? = null,
    val isPast: Boolean = false,
)

@Serializable
data class NamedRefDto(val id: String, val name: String)
