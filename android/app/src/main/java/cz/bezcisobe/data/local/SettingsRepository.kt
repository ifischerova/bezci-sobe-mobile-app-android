package cz.bezcisobe.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cz.bezcisobe.data.remote.TokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : TokenProvider {

    private object Keys {
        val TOKEN = stringPreferencesKey("jwt_token")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme") // LIGHT | DARK | SYSTEM
    }

    val token: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "cs" }
    val theme: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "SYSTEM" }

    override suspend fun currentToken(): String? = context.dataStore.data.first()[Keys.TOKEN]

    suspend fun setToken(value: String?) = context.dataStore.edit {
        if (value == null) it.remove(Keys.TOKEN) else it[Keys.TOKEN] = value
    }

    suspend fun setLanguage(value: String) = context.dataStore.edit { it[Keys.LANGUAGE] = value }
    suspend fun setTheme(value: String) = context.dataStore.edit { it[Keys.THEME] = value }
}
