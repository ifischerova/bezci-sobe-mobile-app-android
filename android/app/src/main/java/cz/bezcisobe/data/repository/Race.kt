package cz.bezcisobe.data.repository

data class Race(
    val id: String,
    val name: String,
    val place: String,
    val date: String,
    val startTime: String?,
    val web: String?,
    val trackLength: String?,
    val trackType: String?,
    val isPast: Boolean,
)
