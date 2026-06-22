package cz.bezcisobe.ui.races

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R
import cz.bezcisobe.ui.components.EmptyState
import cz.bezcisobe.ui.components.LoadingState

@Composable
fun RaceDetailScreen(viewModel: RaceDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        is RaceDetailUiState.Loading -> LoadingState()
        is RaceDetailUiState.NotFound -> EmptyState(stringResource(R.string.race_not_found))
        is RaceDetailUiState.Success -> {
            val r = s.race
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(r.name, style = MaterialTheme.typography.headlineSmall)
                Text("${r.place} • ${r.date}${r.startTime?.let { " $it" } ?: ""}")
                r.trackType?.let { Text(stringResource(R.string.race_type, it)) }
                r.trackLength?.let { Text(stringResource(R.string.race_length, it)) }
                r.web?.let { Text(stringResource(R.string.race_web, it)) }
            }
        }
    }
}
