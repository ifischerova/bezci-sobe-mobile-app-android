package cz.bezcisobe.ui.navigation

object Routes {
    const val RACE_LIST = "races"
    const val RACE_DETAIL = "races/{raceId}"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SETTINGS = "settings"
    fun raceDetail(id: String) = "races/$id"
}
