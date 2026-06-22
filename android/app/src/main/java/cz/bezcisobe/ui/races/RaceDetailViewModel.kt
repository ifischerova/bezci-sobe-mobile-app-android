package cz.bezcisobe.ui.races

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.remote.dto.CreateRideRequestDto
import cz.bezcisobe.data.repository.AuthRepositoryContract
import cz.bezcisobe.data.repository.Race
import cz.bezcisobe.data.repository.RaceRepository
import cz.bezcisobe.data.repository.Ride
import cz.bezcisobe.data.repository.RideRepository
import cz.bezcisobe.data.repository.RideType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

sealed interface RaceDetailUiState {
    data object Loading : RaceDetailUiState
    data class Success(val race: Race) : RaceDetailUiState
    data object NotFound : RaceDetailUiState
}

sealed interface RidesUiState {
    data object Loading : RidesUiState
    data class Success(val rides: List<Ride>) : RidesUiState
    data class Error(val message: String) : RidesUiState
}

@HiltViewModel
class RaceDetailViewModel @Inject constructor(
    private val raceRepository: RaceRepository,
    private val rideRepository: RideRepository,
    auth: AuthRepositoryContract,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val raceId: String = checkNotNull(savedStateHandle["raceId"])

    private val _state = MutableStateFlow<RaceDetailUiState>(RaceDetailUiState.Loading)
    val state: StateFlow<RaceDetailUiState> = _state

    private val _rides = MutableStateFlow<RidesUiState>(RidesUiState.Loading)
    val rides: StateFlow<RidesUiState> = _rides

    /** Server-side validation error from the last create attempt; null clears it. */
    private val _createError = MutableStateFlow<String?>(null)
    val createError: StateFlow<String?> = _createError

    /** True once a create call is in flight, to disable the submit button. */
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    val isLoggedIn: StateFlow<Boolean> =
        auth.isLoggedIn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val currentUserId: StateFlow<String?> =
        auth.currentUserId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentUsername: StateFlow<String?> =
        auth.currentUsername.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            val race = raceRepository.getRace(raceId)
            _state.value = if (race != null) RaceDetailUiState.Success(race) else RaceDetailUiState.NotFound
        }
        loadRides()
    }

    fun loadRides() {
        viewModelScope.launch {
            _rides.value = RidesUiState.Loading
            try {
                _rides.value = RidesUiState.Success(rideRepository.getRides(raceId.toLong()))
            } catch (e: Exception) {
                _rides.value = RidesUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun clearCreateError() { _createError.value = null }

    /**
     * Create an OFFER or REQUEST. For REQUEST, [car] is ignored and seats is forced to 1.
     * Returns via [onSuccess] callback; sets [createError] on a 4xx/validation failure.
     */
    fun createRide(
        type: RideType,
        from: String,
        to: String?,
        car: String?,
        availableSeats: Int,
        notes: String?,
        onSuccess: () -> Unit,
    ) {
        _isSubmitting.value = true
        _createError.value = null
        viewModelScope.launch {
            try {
                val request = CreateRideRequestDto(
                    raceId = raceId.toLong(),
                    type = type.name,
                    from = from.trim(),
                    to = to?.trim()?.ifBlank { null },
                    car = if (type == RideType.OFFER) car?.trim()?.ifBlank { null } else null,
                    availableSeats = if (type == RideType.OFFER) availableSeats else 1,
                    notes = notes?.trim()?.ifBlank { null },
                )
                rideRepository.createRide(request)
                onSuccess()
                loadRides()
            } catch (e: HttpException) {
                _createError.value = parseServerError(e)
            } catch (e: Exception) {
                _createError.value = e.message ?: "Network error"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun acceptRide(id: String) = mutate { rideRepository.acceptRide(id) }
    fun cancelRide(id: String) = mutate { rideRepository.cancelRide(id) }
    fun deleteRide(id: String) = mutate { rideRepository.deleteRide(id) }

    private fun mutate(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (_: Exception) {
                // Surface failures by refreshing; the list reflects the true server state.
            } finally {
                loadRides()
            }
        }
    }

    private fun parseServerError(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            try {
                val json = JSONObject(body)
                for (key in listOf("message", "error", "detail")) {
                    if (json.has(key) && !json.isNull(key)) {
                        val v = json.getString(key)
                        if (v.isNotBlank()) return v
                    }
                }
            } catch (_: Exception) {
                return body
            }
        }
        return e.message()
    }
}
