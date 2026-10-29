package app.ember.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.compose.rememberNavController
import app.ember.focus.domain.repository.CategoryRepository
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.navigation.EmberNavGraph
import app.ember.focus.navigation.Screen
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.EmberTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        CoroutineScope(Dispatchers.IO).launch {
            categoryRepository.ensureDefaultCategories()
        }

        setContent {
            EmberTheme {
                val settings by settingsRepository.settingsFlow.collectAsState(initial = null)
                val navController = rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(BackgroundTop, BackgroundBottom)
                            )
                        )
                ) {
                    if (settings != null) {
                        val startDestination = if (settings?.onboardingCompleted == true) {
                            Screen.Today.route
                        } else {
                            Screen.Onboarding.route
                        }
                        EmberNavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            onFinishActivity = { finish() }
                        )
                    }
                }
            }
        }
    }
}
