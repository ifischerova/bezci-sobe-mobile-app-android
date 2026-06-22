package cz.bezcisobe.di

import android.content.Context
import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.data.remote.TokenProvider
import cz.bezcisobe.data.remote.dto.RaceDto
import cz.bezcisobe.data.repository.AuthRepository
import cz.bezcisobe.data.repository.AuthRepositoryContract
import cz.bezcisobe.data.repository.SampleRaceSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun provideTokenProvider(settings: SettingsRepository): TokenProvider = settings

    @Provides @Singleton
    fun provideAuthRepositoryContract(repo: AuthRepository): AuthRepositoryContract = repo

    @Provides @Singleton
    fun provideSampleRaceSource(
        @ApplicationContext context: Context,
        json: Json,
    ): SampleRaceSource = SampleRaceSource {
        val text = context.assets.open("sample_races.json").bufferedReader().use { it.readText() }
        json.decodeFromString<List<RaceDto>>(text)
    }
}
