package cz.bezcisobe.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    val backStackEntry by nav.currentBackStackEntryAsState()
    val onList = backStackEntry?.destination?.route.let { it == null || it == Routes.RACE_LIST }
    AppScaffold(
        canNavigateBack = !onList,
        onBack = { nav.popBackStack() },
        onHome = {
            if (!onList) {
                nav.navigate(Routes.RACE_LIST) {
                    popUpTo(Routes.RACE_LIST) { inclusive = false }
                    launchSingleTop = true
                }
            }
        },
        onLogin = { nav.navigate(Routes.LOGIN) },
        onSettings = { nav.navigate(Routes.SETTINGS) },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.RACE_LIST,
            modifier = Modifier.padding(padding),
        ) {
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
}
