package cz.bezcisobe.ui.races

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.repository.Race
import cz.bezcisobe.data.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RaceListUiState {
    data object Loading : RaceListUiState
    data class Success(val races: List<Race>) : RaceListUiState
    data class Error(val message: String) : RaceListUiState
}

@HiltViewModel
class RaceListViewModel @Inject constructor(
    private val repository: RaceRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<RaceListUiState> =
        combine(repository.observeRaces(), query, errorMessage) { races, q, err ->
            when {
                err != null && races.isEmpty() -> RaceListUiState.Error(err)
                else -> RaceListUiState.Success(
                    races.filter { it.name.contains(q, true) || it.place.contains(q, true) }
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RaceListUiState.Loading)

    init { refresh() }

    fun onSearchChange(value: String) { query.value = value }

    fun refresh() {
        viewModelScope.launch {
            try { repository.refresh(); errorMessage.value = null }
            catch (e: Exception) { errorMessage.value = e.message ?: "Network error" }
        }
    }
}
