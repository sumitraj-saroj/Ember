package app.ember.focus.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.ember.focus.BuildConfig
import app.ember.focus.ui.theme.TextCream

@Composable
fun EmberNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Today.route,
    onFinishActivity: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            app.ember.focus.ui.onboarding.OnboardingScreen(
                settingsRepository = (LocalContext.current.applicationContext as app.ember.focus.EmberApp).let {
                    dagger.hilt.android.EntryPointAccessors.fromApplication(
                        it,
                        app.ember.focus.widget.EmberWidget.WidgetEntryPoint::class.java
                    ).settingsRepository()
                },
                onFinishOnboarding = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Today.route) {
            app.ember.focus.ui.today.TodayScreen(
                onNavigateToStartSession = { navController.navigate(Screen.StartSession.route) },
                onNavigateToFocus = { navController.navigate(Screen.Focus.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToCandleLab = { navController.navigate(Screen.CandleLab.route) }
            )
        }
        composable(
            route = Screen.StartSession.route,
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) },
            exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) }
        ) {
            app.ember.focus.ui.today.StartSessionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFocus = {
                    navController.navigate(Screen.Focus.route) {
                        popUpTo(Screen.StartSession.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Focus.route) {
            app.ember.focus.ui.focus.FocusScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Stats.route) {
            app.ember.focus.ui.stats.StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            app.ember.focus.ui.settings.SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        if (BuildConfig.DEBUG) {
            composable(Screen.CandleLab.route) {
                app.ember.focus.ui.candle.CandleLabScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name, color = TextCream)
    }
}
