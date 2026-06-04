package cz.bezcisobe.di

import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.data.remote.TokenProvider
import cz.bezcisobe.data.repository.AuthRepository
import cz.bezcisobe.data.repository.AuthRepositoryContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun provideTokenProvider(settings: SettingsRepository): TokenProvider = settings

    @Provides @Singleton
    fun provideAuthRepositoryContract(repo: AuthRepository): AuthRepositoryContract = repo
}
