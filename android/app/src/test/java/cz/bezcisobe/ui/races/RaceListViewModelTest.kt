package cz.bezcisobe.ui.races

import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.local.RaceEntity
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.NamedRefDto
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.repository.RaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
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

class RaceListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun teardown() = Dispatchers.resetMain()

    private val dto = RaceDto(id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
        trackLength = NamedRefDto("l", "10K"), trackType = NamedRefDto("t", "Road"))

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = RaceListViewModel(RaceRepository(FakeApi(listOf(dto)), FakeDao()))
        assertTrue(vm.state.value is RaceListUiState.Loading)
    }

    @Test
    fun `loads races into Success state`() = runTest {
        val vm = RaceListViewModel(RaceRepository(FakeApi(listOf(dto)), FakeDao()))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        advanceUntilIdle()
        val success = vm.state.value as RaceListUiState.Success
        assertEquals(1, success.races.size)
        assertEquals("Praha 10K", success.races[0].name)
    }

    @Test
    fun `search filters by name`() = runTest {
        val vm = RaceListViewModel(RaceRepository(FakeApi(listOf(dto)), FakeDao()))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        advanceUntilIdle()
        vm.onSearchChange("brno")
        advanceUntilIdle()
        val state = vm.state.value as RaceListUiState.Success
        assertEquals(0, state.races.size)
    }
}
