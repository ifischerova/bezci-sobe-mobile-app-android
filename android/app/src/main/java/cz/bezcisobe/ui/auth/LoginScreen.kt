package cz.bezcisobe.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onRegister: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state) { if (state is AuthUiState.Success) { viewModel.reset(); onLoggedIn() } }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.login), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(username, { username = it }, label = { Text(stringResource(R.string.username)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        (state as? AuthUiState.Error)?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { viewModel.login(username, password) }, enabled = state !is AuthUiState.Loading, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.login))
        }
        TextButton(onClick = onRegister) { Text(stringResource(R.string.no_account_register)) }
    }
}
