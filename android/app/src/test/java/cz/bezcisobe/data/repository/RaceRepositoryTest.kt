package cz.bezcisobe.data.repository

import app.cash.turbine.test
import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.local.RaceEntity
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.NamedRefDto
import cz.bezcisobe.data.remote.dto.RaceDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeDao : RaceDao {
    val state = MutableStateFlow<List<RaceEntity>>(emptyList())
    override fun observeRaces(): Flow<List<RaceEntity>> = state
    override suspend fun getById(id: String) = state.value.find { it.id == id }
    override suspend fun count() = state.value.size
    override suspend fun upsertAll(races: List<RaceEntity>) { state.value = races }
    override suspend fun clear() { state.value = emptyList() }
}

private class FakeApi(
    private val races: List<RaceDto>,
    private val fail: Boolean = false,
) : ApiService {
    override suspend fun getRaces(): List<RaceDto> = if (fail) throw RuntimeException("offline") else races
    override suspend fun getRace(id: String) = races.first { it.id == id }
    override suspend fun login(body: cz.bezcisobe.data.remote.dto.LoginRequestDto) = throw NotImplementedError()
    override suspend fun register(body: cz.bezcisobe.data.remote.dto.RegisterRequestDto) = throw NotImplementedError()
    override suspend fun me() = throw NotImplementedError()
    override suspend fun getRides(raceId: Long) = throw NotImplementedError()
    override suspend fun createRide(body: cz.bezcisobe.data.remote.dto.CreateRideRequestDto) = throw NotImplementedError()
    override suspend fun deleteRide(id: String) = throw NotImplementedError()
    override suspend fun acceptRide(id: String) = throw NotImplementedError()
    override suspend fun cancelRide(id: String) = throw NotImplementedError()
}

class RaceRepositoryTest {
    private val dto = RaceDto(
        id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
        startTime = "10:00", trackLength = NamedRefDto("l", "10K"), trackType = NamedRefDto("t", "Road"),
    )
    private val sample = RaceDto(id = "s1", name = "Bundled Race", place = "Brno", date = "2026-08-01")
    private val emptySource = SampleRaceSource { emptyList() }
    private val sampleSource = SampleRaceSource { listOf(sample) }

    @Test
    fun `refresh stores api data and observe emits it`() = runTest {
        val dao = FakeDao()
        val repo = RaceRepository(FakeApi(listOf(dto)), dao, emptySource)
        repo.refresh()
        repo.observeRaces().test {
            val races = awaitItem()
            assertEquals(1, races.size)
            assertEquals("Praha 10K", races[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh seeds bundled sample data when api fails and cache empty`() = runTest {
        val dao = FakeDao()
        val repo = RaceRepository(FakeApi(emptyList(), fail = true), dao, sampleSource)
        repo.refresh()
        assertEquals(1, dao.count())
        assertEquals("Bundled Race", dao.state.value[0].name)
    }

    @Test
    fun `refresh rethrows when api fails but cache is non-empty`() = runTest {
        val dao = FakeDao()
        dao.state.value = listOf(
            RaceEntity("1", "Cached", "Praha", "2026-07-01", null, null, null, null, false),
        )
        val repo = RaceRepository(FakeApi(emptyList(), fail = true), dao, sampleSource)
        try {
            repo.refresh()
            throw AssertionError("expected refresh to rethrow")
        } catch (e: RuntimeException) {
            assertEquals("offline", e.message)
        }
        // existing cache untouched, sample NOT applied
        assertEquals(1, dao.count())
        assertEquals("Cached", dao.state.value[0].name)
    }
}
