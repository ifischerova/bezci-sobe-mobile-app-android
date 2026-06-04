package cz.bezcisobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cz.bezcisobe.ui.navigation.BezciNavGraph
import cz.bezcisobe.ui.theme.BezciSobeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BezciSobeTheme { BezciNavGraph() } }
    }
}
