package cz.bezcisobe.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R
import cz.bezcisobe.ui.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    onLogin: () -> Unit,
    onSettings: () -> Unit,
    session: SessionViewModel = hiltViewModel(),
    content: @Composable (PaddingValues) -> Unit,
) {
    val loggedIn by session.isLoggedIn.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                if (loggedIn) TextButton(onClick = { session.logout() }) { Text(stringResource(R.string.logout)) }
                else TextButton(onClick = onLogin) { Text(stringResource(R.string.login)) }
                TextButton(onClick = onSettings) { Text(stringResource(R.string.settings)) }
            },
        )
    }) { padding -> content(padding) }
}
