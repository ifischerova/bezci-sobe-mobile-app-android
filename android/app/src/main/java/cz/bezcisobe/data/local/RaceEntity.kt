package cz.bezcisobe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "races")
data class RaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val place: String,
    val date: String,
    val startTime: String?,
    val web: String?,
    val trackLength: String?,
    val trackType: String?,
    val isPast: Boolean,
)
