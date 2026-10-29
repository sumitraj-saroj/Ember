package app.ember.focus.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Today : Screen("today")
    data object Focus : Screen("focus")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    data object CandleLab : Screen("candle_lab")
}
