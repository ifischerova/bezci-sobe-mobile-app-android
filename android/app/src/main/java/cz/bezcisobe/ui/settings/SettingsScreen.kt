package cz.bezcisobe.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
        listOf("LIGHT" to R.string.theme_light, "DARK" to R.string.theme_dark, "SYSTEM" to R.string.theme_system).forEach { (value, label) ->
            Row(
                Modifier.fillMaxWidth().selectable(selected = theme == value, onClick = { viewModel.setTheme(value) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = theme == value, onClick = { viewModel.setTheme(value) })
                Text(stringResource(label))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
        listOf("cs" to "Čeština", "en" to "English").forEach { (value, label) ->
            Row(
                Modifier.fillMaxWidth().selectable(selected = language == value, onClick = { viewModel.setLanguage(value) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = language == value, onClick = { viewModel.setLanguage(value) })
                Text(label)
            }
        }
    }
}
