package cz.bezcisobe.data.mapper

import cz.bezcisobe.data.remote.dto.NamedRefDto
import cz.bezcisobe.data.remote.dto.RaceDto
import org.junit.Assert.assertEquals
import org.junit.Test

class RaceMappersTest {
    @Test
    fun `dto maps to entity flattening named refs`() {
        val dto = RaceDto(
            id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
            startTime = "10:00", web = null,
            trackLength = NamedRefDto("l1", "10K"),
            trackType = NamedRefDto("t1", "Road"),
            certifications = emptyList(), raceCalendarId = "c1", isPast = false,
        )
        val entity = dto.toEntity()
        assertEquals("1", entity.id)
        assertEquals("10K", entity.trackLength)
        assertEquals("Road", entity.trackType)
    }

    @Test
    fun `entity maps to domain model`() {
        val entity = cz.bezcisobe.data.local.RaceEntity(
            id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
            startTime = "10:00", web = null, trackLength = "10K", trackType = "Road", isPast = false,
        )
        val race = entity.toDomain()
        assertEquals("Praha 10K", race.name)
        assertEquals("10K", race.trackLength)
    }
}
