package cz.bezcisobe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.repository.AuthRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val auth: AuthRepositoryContract,
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> =
        auth.isLoggedIn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    fun logout() { viewModelScope.launch { auth.logout() } }
}
