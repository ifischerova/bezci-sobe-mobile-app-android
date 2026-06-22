package cz.bezcisobe.data.repository

import cz.bezcisobe.data.mapper.toDomain
import cz.bezcisobe.data.remote.dto.RideDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RideTest {

    private fun dto(
        type: String = "OFFER",
        firstName: String? = "Jana",
        lastName: String? = "Nováková",
        username: String = "jnovak",
        availableSeats: Int = 3,
        occupiedSeats: Int = 1,
    ) = RideDto(
        id = "r1",
        raceId = "1",
        userId = "u1",
        userUsername = username,
        userFirstName = firstName,
        userLastName = lastName,
        type = type,
        from = "Brno",
        availableSeats = availableSeats,
        occupiedSeats = occupiedSeats,
        createdAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun `toDomain maps OFFER and REQUEST case-insensitively`() {
        assertEquals(RideType.OFFER, dto(type = "OFFER").toDomain().type)
        assertEquals(RideType.OFFER, dto(type = "offer").toDomain().type)
        assertEquals(RideType.REQUEST, dto(type = "REQUEST").toDomain().type)
        // Unknown/blank types fall back to REQUEST.
        assertEquals(RideType.REQUEST, dto(type = "whatever").toDomain().type)
    }

    @Test
    fun `freeSeats subtracts occupied and never goes negative`() {
        assertEquals(2, dto(availableSeats = 3, occupiedSeats = 1).toDomain().freeSeats)
        assertEquals(0, dto(availableSeats = 2, occupiedSeats = 5).toDomain().freeSeats)
    }

    @Test
    fun `isFull is true only for an OFFER with no free seats`() {
        assertTrue(dto(type = "OFFER", availableSeats = 2, occupiedSeats = 2).toDomain().isFull)
        assertFalse(dto(type = "OFFER", availableSeats = 3, occupiedSeats = 1).toDomain().isFull)
        // A REQUEST is never "full" regardless of seat counts.
        assertFalse(dto(type = "REQUEST", availableSeats = 0, occupiedSeats = 0).toDomain().isFull)
    }

    @Test
    fun `displayName prefers full name and falls back to username`() {
        assertEquals("Jana Nováková", dto().toDomain().displayName)
        assertEquals("Jana", dto(lastName = null).toDomain().displayName)
        assertEquals("jnovak", dto(firstName = null, lastName = null).toDomain().displayName)
        assertEquals("jnovak", dto(firstName = "  ", lastName = "").toDomain().displayName)
    }
}
