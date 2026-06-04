package cz.bezcisobe.di

import android.content.Context
import androidx.room.Room
import cz.bezcisobe.data.local.AppDatabase
import cz.bezcisobe.data.local.RaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "bezcisobe.db").build()

    @Provides
    fun provideRaceDao(db: AppDatabase): RaceDao = db.raceDao()
}
