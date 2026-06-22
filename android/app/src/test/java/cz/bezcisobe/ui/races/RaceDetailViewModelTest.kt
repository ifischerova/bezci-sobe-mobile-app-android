package cz.bezcisobe.ui.races

import androidx.lifecycle.SavedStateHandle
import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.local.RaceEntity
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.repository.RaceRepository
import cz.bezcisobe.data.repository.SampleRaceSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class DetailFakeDao(initial: List<RaceEntity>) : RaceDao {
    val state = MutableStateFlow(initial)
    override fun observeRaces(): Flow<List<RaceEntity>> = state
    override suspend fun getById(id: String) = state.value.find { it.id == id }
    override suspend fun count() = state.value.size
    override suspend fun upsertAll(races: List<RaceEntity>) { state.value = races }
    override suspend fun clear() { state.value = emptyList() }
}
private class DetailFakeApi : ApiService {
    override suspend fun getRaces(): List<RaceDto> = emptyList()
    override suspend fun getRace(id: String) = throw NotImplementedError()
    override suspend fun login(body: cz.bezcisobe.data.remote.dto.LoginRequestDto) = throw NotImplementedError()
    override suspend fun register(body: cz.bezcisobe.data.remote.dto.RegisterRequestDto) = throw NotImplementedError()
    override suspend fun me() = throw NotImplementedError()
}

class RaceDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun teardown() = Dispatchers.resetMain()

    private val emptySource = SampleRaceSource { emptyList() }
    private val entity = RaceEntity(
        id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
        startTime = "10:00", web = null, trackLength = "10K", trackType = "Road", isPast = false,
    )

    private fun repo(dao: RaceDao) = RaceRepository(DetailFakeApi(), dao, emptySource)

    @Test
    fun `Success when race found in cache`() = runTest {
        val vm = RaceDetailViewModel(repo(DetailFakeDao(listOf(entity))), SavedStateHandle(mapOf("raceId" to "1")))
        advanceUntilIdle()
        val state = vm.state.value
        assertTrue(state is RaceDetailUiState.Success)
        assertEquals("Praha 10K", (state as RaceDetailUiState.Success).race.name)
    }

    @Test
    fun `NotFound when race id absent`() = runTest {
        val vm = RaceDetailViewModel(repo(DetailFakeDao(emptyList())), SavedStateHandle(mapOf("raceId" to "999")))
        advanceUntilIdle()
        assertTrue(vm.state.value is RaceDetailUiState.NotFound)
    }
}
