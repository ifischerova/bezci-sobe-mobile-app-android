package cz.bezcisobe.data.repository

import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.data.remote.ApiService
import cz.bezcisobe.data.remote.dto.LoginRequestDto
import cz.bezcisobe.data.remote.dto.RegisterRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val settings: SettingsRepository,
) {
    val isLoggedIn: Flow<Boolean> = settings.token.map { !it.isNullOrBlank() }

    suspend fun login(username: String, password: String) {
        val res = api.login(LoginRequestDto(username, password))
        settings.setToken(res.token)
    }

    suspend fun register(username: String, email: String, password: String, language: String) {
        api.register(RegisterRequestDto(username, email, password, language))
    }

    suspend fun logout() = settings.setToken(null)
}
