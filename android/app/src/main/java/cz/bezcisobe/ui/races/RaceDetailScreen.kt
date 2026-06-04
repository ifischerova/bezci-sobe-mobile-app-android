package cz.bezcisobe.ui.races

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.ui.components.LoadingState

@Composable
fun RaceDetailScreen(viewModel: RaceDetailViewModel = hiltViewModel()) {
    val race by viewModel.race.collectAsStateWithLifecycle()
    val r = race
    if (r == null) { LoadingState(); return }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(r.name, style = MaterialTheme.typography.headlineSmall)
        Text("${r.place} • ${r.date}${r.startTime?.let { " $it" } ?: ""}")
        r.trackType?.let { Text("Typ: $it") }
        r.trackLength?.let { Text("Délka: $it") }
        r.web?.let { Text("Web: $it") }
    }
}
