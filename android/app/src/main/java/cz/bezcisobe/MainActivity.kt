package cz.bezcisobe

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.ui.navigation.BezciNavGraph
import cz.bezcisobe.ui.theme.BezciSobeTheme
import cz.bezcisobe.work.UpcomingRaceWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Only run the upcoming-race sync/notification when there is connectivity.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val upcomingWork = PeriodicWorkRequestBuilder<UpcomingRaceWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "upcoming_races", ExistingPeriodicWorkPolicy.KEEP, upcomingWork,
        )
        setContent {
            val theme by settings.theme.collectAsState(initial = "SYSTEM")
            val dark = when (theme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            // Language is applied via AppCompatDelegate.setApplicationLocales (see
            // BezciSobeApplication / SettingsViewModel), which recreates the activity with
            // the correct per-app locale, so stringResource() resolves normally here.
            BezciSobeTheme(darkTheme = dark) { BezciNavGraph() }
        }
    }
}
