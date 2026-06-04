package cz.bezcisobe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cz.bezcisobe.ui.auth.LoginScreen
import cz.bezcisobe.ui.auth.RegisterScreen
import cz.bezcisobe.ui.races.RaceDetailScreen
import cz.bezcisobe.ui.races.RaceListScreen
import cz.bezcisobe.ui.settings.SettingsScreen

@Composable
fun BezciNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.RACE_LIST) {
        composable(Routes.RACE_LIST) {
            RaceListScreen(onRaceClick = { id -> nav.navigate(Routes.raceDetail(id)) })
        }
        composable(Routes.RACE_DETAIL, arguments = listOf(navArgument("raceId") { type = NavType.StringType })) {
            RaceDetailScreen()
        }
        composable(Routes.LOGIN) { LoginScreen(onLoggedIn = { nav.popBackStack() }, onRegister = { nav.navigate(Routes.REGISTER) }) }
        composable(Routes.REGISTER) { RegisterScreen(onRegistered = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}
