package app.ember.focus.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AmberAccent,
    onPrimary = TextDark,
    primaryContainer = AmberDark,
    onPrimaryContainer = TextCream,
    secondary = PrimaryPillFill,
    onSecondary = PrimaryPillText,
    secondaryContainer = SecondaryPillFill,
    onSecondaryContainer = SecondaryPillText,
    tertiary = AmberGlow,
    background = BackgroundTop,
    onBackground = TextCream,
    surface = SurfaceCard,
    onSurface = TextCream,
    surfaceVariant = SurfaceCardElevated,
    onSurfaceVariant = TextMuted,
    outline = SurfaceBorder,
    outlineVariant = SecondaryPillBorder,
    error = AshGray,
    onError = TextCream
)

@Composable
fun EmberTheme(
    fontPreset: String = "MANROPE",
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val emberTypography = remember(fontPreset) { getEmberTypography(fontPreset) }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalEmberTypography provides emberTypography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = emberTypography.material,
            content = content
        )
    }
}
