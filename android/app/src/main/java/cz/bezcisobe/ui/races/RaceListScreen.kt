package cz.bezcisobe.ui.races

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R
import cz.bezcisobe.ui.components.*

@Composable
fun RaceListScreen(onRaceClick: (String) -> Unit, viewModel: RaceListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var search by rememberSaveable { mutableStateOf("") }
    RaceListContent(
        state = state,
        search = search,
        onSearch = { search = it; viewModel.onSearchChange(it) },
        onRetry = viewModel::refresh,
        onRaceClick = onRaceClick,
    )
}

@Composable
fun RaceListContent(
    state: RaceListUiState,
    search: String,
    onSearch: (String) -> Unit,
    onRetry: () -> Unit,
    onRaceClick: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = search,
            onValueChange = onSearch,
            label = { Text(stringResource(R.string.search_races)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
        when (val s = state) {
            is RaceListUiState.Loading -> LoadingState()
            is RaceListUiState.Error -> ErrorState(s.message, onRetry = onRetry)
            is RaceListUiState.Success ->
                if (s.races.isEmpty()) EmptyState(stringResource(R.string.no_races))
                else LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(s.races, key = { it.id }) { race ->
                        RaceCard(race = race, onClick = { onRaceClick(race.id) })
                    }
                }
        }
    }
}
