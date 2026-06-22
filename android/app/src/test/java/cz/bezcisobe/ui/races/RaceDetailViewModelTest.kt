package cz.bezcisobe.ui.races

import androidx.lifecycle.SavedStateHandle
import cz.bezcisobe.data.local.RaceDao
import cz.bezcisobe.data.local.RaceEntity
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.CreateRideRequestDto
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.remote.dto.RideDto
import cz.bezcisobe.data.repository.RaceRepository
import cz.bezcisobe.data.repository.RideRepository
import cz.bezcisobe.data.repository.RideType
import cz.bezcisobe.data.repository.SampleRaceSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

private class DetailFakeDao(initial: List<RaceEntity>) : RaceDao {
    val state = MutableStateFlow(initial)
    override fun observeRaces(): Flow<List<RaceEntity>> = state
    override suspend fun getById(id: String) = state.value.find { it.id == id }
    override suspend fun count() = state.value.size
    override suspend fun upsertAll(races: List<RaceEntity>) { state.value = races }
    override suspend fun clear() { state.value = emptyList() }
}

/**
 * Configurable fake API for the ride flows. Records calls and can be made to fail
 * so the ViewModel's error paths are exercised, not just the happy path.
 */
private class DetailFakeApi(
    var rides: List<RideDto> = emptyList(),
    var createError: Throwable? = null,
    var acceptError: Throwable? = null,
) : ApiService {
    var getRidesCalls = 0
    var createdRequest: CreateRideRequestDto? = null

    override suspend fun getRaces(): List<RaceDto> = emptyList()
    override suspend fun getRace(id: String) = throw NotImplementedError()
    override suspend fun login(body: cz.bezcisobe.data.remote.dto.LoginRequestDto) = throw NotImplementedError()
    override suspend fun register(body: cz.bezcisobe.data.remote.dto.RegisterRequestDto) = throw NotImplementedError()
    override suspend fun me() = throw NotImplementedError()

    override suspend fun getRides(raceId: Long): List<RideDto> {
        getRidesCalls++
        return rides
    }

    override suspend fun createRide(body: CreateRideRequestDto): RideDto {
        createError?.let { throw it }
        createdRequest = body
        return rideDto(id = "new", type = body.type)
    }

    override suspend fun deleteRide(id: String) {}

    override suspend fun acceptRide(id: String): RideDto {
        acceptError?.let { throw it }
        return rides.first { it.id == id }
    }

    override suspend fun cancelRide(id: String): RideDto = rides.first { it.id == id }
}

private class DetailFakeAuth : cz.bezcisobe.data.repository.AuthRepositoryContract {
    override val isLoggedIn = MutableStateFlow(false)
    override val currentUserId = MutableStateFlow<String?>(null)
    override val currentUsername = MutableStateFlow<String?>(null)
    override suspend fun login(username: String, password: String) {}
    override suspend fun register(username: String, email: String, password: String, language: String) {}
    override suspend fun logout() {}
}

private fun rideDto(
    id: String,
    type: String,
    userId: String = "u1",
    availableSeats: Int = 3,
    occupiedSeats: Int = 0,
    passengers: List<String> = emptyList(),
) = RideDto(
    id = id,
    raceId = "1",
    userId = userId,
    userUsername = "user_$userId",
    type = type,
    from = "Brno",
    availableSeats = availableSeats,
    occupiedSeats = occupiedSeats,
    passengers = passengers,
    createdAt = "2026-01-01T00:00:00Z",
)

private fun httpError(status: Int, body: String): HttpException =
    HttpException(Response.error<Any>(status, body.toResponseBody("application/json".toMediaType())))

@OptIn(ExperimentalCoroutinesApi::class)
class RaceDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun teardown() = Dispatchers.resetMain()

    private val emptySource = SampleRaceSource { emptyList() }
    private val entity = RaceEntity(
        id = "1", name = "Praha 10K", place = "Praha", date = "2026-07-01",
        startTime = "10:00", web = null, trackLength = "10K", trackType = "Road", isPast = false,
    )

    private fun raceRepo(dao: RaceDao, api: ApiService) = RaceRepository(api, dao, emptySource)

    private fun viewModel(
        api: DetailFakeApi,
        raceId: String = "1",
        races: List<RaceEntity> = listOf(entity),
    ) = RaceDetailViewModel(
        raceRepo(DetailFakeDao(races), api),
        RideRepository(api),
        DetailFakeAuth(),
        SavedStateHandle(mapOf("raceId" to raceId)),
    )

    @Test
    fun `Success when race found in cache`() = runTest {
        val vm = viewModel(DetailFakeApi())
        advanceUntilIdle()
        val state = vm.state.value
        assertTrue(state is RaceDetailUiState.Success)
        assertEquals("Praha 10K", (state as RaceDetailUiState.Success).race.name)
    }

    @Test
    fun `NotFound when race id absent`() = runTest {
        val vm = viewModel(DetailFakeApi(), races = emptyList(), raceId = "999")
        advanceUntilIdle()
        assertTrue(vm.state.value is RaceDetailUiState.NotFound)
    }

    @Test
    fun `rides load and are exposed as Success`() = runTest {
        val api = DetailFakeApi(
            rides = listOf(
                rideDto(id = "o1", type = "OFFER"),
                rideDto(id = "r1", type = "REQUEST"),
            ),
        )
        val vm = viewModel(api)
        advanceUntilIdle()
        val rides = vm.rides.value
        assertTrue(rides is RidesUiState.Success)
        val list = (rides as RidesUiState.Success).rides
        assertEquals(2, list.size)
        assertEquals(1, list.count { it.type == RideType.OFFER })
        assertEquals(1, list.count { it.type == RideType.REQUEST })
    }

    @Test
    fun `non-numeric race id yields rides Error without crashing`() = runTest {
        val vm = viewModel(DetailFakeApi(), races = emptyList(), raceId = "abc")
        advanceUntilIdle()
        assertTrue(vm.rides.value is RidesUiState.Error)
    }

    @Test
    fun `createRide happy path toggles submitting, reloads, and invokes onSuccess`() = runTest {
        val api = DetailFakeApi()
        val vm = viewModel(api)
        advanceUntilIdle()
        val ridesCallsBefore = api.getRidesCalls
        var onSuccessCalled = false

        vm.createRide(RideType.OFFER, from = "Brno", to = "Praha", car = "Octavia", availableSeats = 3, notes = null) {
            onSuccessCalled = true
        }
        advanceUntilIdle()

        assertTrue(onSuccessCalled)
        assertFalse(vm.isSubmitting.value)
        assertNull(vm.createError.value)
        assertEquals("OFFER", api.createdRequest?.type)
        assertEquals(1L, api.createdRequest?.raceId)
        assertEquals(ridesCallsBefore + 1, api.getRidesCalls) // reloaded after create
    }

    @Test
    fun `createRide surfaces server message on HttpException`() = runTest {
        val api = DetailFakeApi(createError = httpError(400, """{"message":"Race already passed"}"""))
        val vm = viewModel(api)
        advanceUntilIdle()
        var onSuccessCalled = false

        vm.createRide(RideType.OFFER, from = "Brno", to = null, car = "Octavia", availableSeats = 2, notes = null) {
            onSuccessCalled = true
        }
        advanceUntilIdle()

        assertFalse(onSuccessCalled)
        assertFalse(vm.isSubmitting.value)
        // On-device org.json parses out "Race already passed"; the local JVM stub falls
        // back to the raw body. Either way the message must be surfaced to createError.
        assertTrue(vm.createError.value!!.contains("Race already passed"))
    }

    @Test
    fun `accept failure sets actionError and still refreshes`() = runTest {
        val api = DetailFakeApi(
            rides = listOf(rideDto(id = "o1", type = "OFFER")),
            acceptError = httpError(409, """{"message":"Ride is full"}"""),
        )
        val vm = viewModel(api)
        advanceUntilIdle()
        val ridesCallsBefore = api.getRidesCalls

        vm.acceptRide("o1")
        advanceUntilIdle()

        assertTrue(vm.actionError.value!!.contains("Ride is full"))
        assertEquals(ridesCallsBefore + 1, api.getRidesCalls) // refreshed despite failure

        vm.clearActionError()
        assertNull(vm.actionError.value)
    }
}
