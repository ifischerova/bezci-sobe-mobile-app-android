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

@HiltViewModel
class RaceDetailViewModel @Inject constructor(
    repository: RaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _race = MutableStateFlow<Race?>(null)
    val race: StateFlow<Race?> = _race
    init {
        val id: String = checkNotNull(savedStateHandle["raceId"])
        viewModelScope.launch { _race.value = repository.getRace(id) }
    }
}
