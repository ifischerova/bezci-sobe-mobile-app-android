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
    override suspend fun upsertAll(races: List<RaceEntity>) { state.value = races }
    override suspend fun clear() { state.value = emptyList() }
}

private class FakeApi(private val races: List<RaceDto>) : ApiService {
    override suspend fun getRaces() = races
    override suspend fun getRace(id: String) = races.first { it.id == id }
    override suspend fun login(body: cz.bezcisobe.data.remote.dto.LoginRequestDto) = throw NotImplementedError()
    override suspend fun register(body: cz.bezcisobe.data.remote.dto.RegisterRequestDto) = throw NotImplementedError()
    override suspend fun me() = throw NotImplementedError()
}

class RaceRepositoryTest {
    private val dto = RaceDto(
        id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
        startTime = "10:00", trackLength = NamedRefDto("l", "10K"), trackType = NamedRefDto("t", "Road"),
    )

    @Test
    fun `refresh stores api data and observe emits it`() = runTest {
        val dao = FakeDao()
        val repo = RaceRepository(FakeApi(listOf(dto)), dao)
        repo.refresh()
        repo.observeRaces().test {
            val races = awaitItem()
            assertEquals(1, races.size)
            assertEquals("Praha 10K", races[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
