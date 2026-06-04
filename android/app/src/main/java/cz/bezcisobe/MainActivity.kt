package cz.bezcisobe

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import cz.bezcisobe.data.local.SettingsRepository
import cz.bezcisobe.ui.navigation.BezciNavGraph
import cz.bezcisobe.ui.theme.BezciSobeTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalContext provides localizedContext,
            ) {
                BezciSobeTheme(darkTheme = dark) { BezciNavGraph() }
            }
        }
    }
}
