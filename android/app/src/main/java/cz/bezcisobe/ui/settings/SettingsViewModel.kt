package cz.bezcisobe.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.bezcisobe.data.local.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {
    val theme: StateFlow<String> = settings.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SYSTEM")
    val language: StateFlow<String> = settings.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "cs")
    fun setTheme(value: String) { viewModelScope.launch { settings.setTheme(value) } }
    fun setLanguage(value: String) { viewModelScope.launch { settings.setLanguage(value) } }
}
