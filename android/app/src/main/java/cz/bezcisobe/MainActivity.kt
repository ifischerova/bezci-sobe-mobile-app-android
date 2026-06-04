package cz.bezcisobe

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.ui.navigation.BezciNavGraph
import cz.bezcisobe.ui.theme.BezciSobeTheme
import cz.bezcisobe.work.UpcomingRaceWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        val upcomingWork = PeriodicWorkRequestBuilder<UpcomingRaceWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "upcoming_races", ExistingPeriodicWorkPolicy.KEEP, upcomingWork,
        )
        setContent {
            val theme by settings.theme.collectAsState(initial = "SYSTEM")
            val lang by settings.language.collectAsState(initial = "cs")
            val dark = when (theme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            val baseContext = LocalContext.current
            val localizedConfig = remember(lang) {
                Configuration(baseContext.resources.configuration).apply { setLocale(Locale(lang)) }
            }
            val localizedContext = remember(lang) { baseContext.createConfigurationContext(localizedConfig) }
            // NOTE: do NOT override LocalContext with createConfigurationContext — that
            // returns a plain ContextImpl (not the Activity) and breaks hiltViewModel()
            // ("Expected an activity context for creating a HiltViewModelFactory").
            // stringResource reads LocalResources in this Compose version, so overriding
            // LocalResources + LocalConfiguration is enough to switch the in-app language.
            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalResources provides localizedContext.resources,
            ) {
                BezciSobeTheme(darkTheme = dark) { BezciNavGraph() }
            }
        }
    }
}
