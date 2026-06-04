package cz.bezcisobe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.bezcisobe.data.repository.Race

@Composable
fun RaceCard(race: Race, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(race.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${race.place} • ${race.date}", style = MaterialTheme.typography.bodyMedium)
            race.trackLength?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
        }
    }
}
