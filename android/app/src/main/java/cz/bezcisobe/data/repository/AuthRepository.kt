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
) : AuthRepositoryContract {
    override val isLoggedIn: Flow<Boolean> = settings.token.map { !it.isNullOrBlank() }
    override val currentUserId: Flow<String?> = settings.userId
    override val currentUsername: Flow<String?> = settings.username

    override suspend fun login(username: String, password: String) {
        val res = api.login(LoginRequestDto(username, password))
        settings.setToken(res.token)
        settings.setSession(res.userId, res.username)
    }

    override suspend fun register(username: String, email: String, password: String, language: String) {
        api.register(RegisterRequestDto(username, email, password, language))
    }

    override suspend fun logout() {
        settings.setToken(null)
        settings.setSession(null, null)
    }
}
