package cz.bezcisobe.data.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepositoryContract {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(username: String, password: String)
    suspend fun register(username: String, email: String, password: String, language: String)
    suspend fun logout()
}
