package cz.bezcisobe.ui.races

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.repository.Race
import cz.bezcisobe.data.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RaceDetailUiState {
    data object Loading : RaceDetailUiState
    data class Success(val race: Race) : RaceDetailUiState
    data object NotFound : RaceDetailUiState
}

@HiltViewModel
class RaceDetailViewModel @Inject constructor(
    repository: RaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow<RaceDetailUiState>(RaceDetailUiState.Loading)
    val state: StateFlow<RaceDetailUiState> = _state

    init {
        val id: String = checkNotNull(savedStateHandle["raceId"])
        viewModelScope.launch {
            val race = repository.getRace(id)
            _state.value = if (race != null) RaceDetailUiState.Success(race) else RaceDetailUiState.NotFound
        }
    }
}
