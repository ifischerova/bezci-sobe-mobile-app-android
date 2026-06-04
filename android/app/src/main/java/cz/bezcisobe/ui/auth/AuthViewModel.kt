package cz.bezcisobe.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.repository.AuthRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepositoryContract,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(username: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try { repository.login(username, password); _uiState.value = AuthUiState.Success }
            catch (e: Exception) { _uiState.value = AuthUiState.Error(e.message ?: "Přihlášení selhalo") }
        }
    }

    fun register(username: String, email: String, password: String, language: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try { repository.register(username, email, password, language); _uiState.value = AuthUiState.Success }
            catch (e: Exception) { _uiState.value = AuthUiState.Error(e.message ?: "Registrace selhala") }
        }
    }

    fun reset() { _uiState.value = AuthUiState.Idle }
}
