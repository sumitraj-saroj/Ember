package app.ember.focus.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ember.focus.R

@OptIn(ExperimentalTextApi::class)
val ManropeFontFamily = FontFamily(
    Font(
        R.font.manrope,
        weight = FontWeight.ExtraLight,
        variationSettings = FontVariation.Settings(FontVariation.weight(200))
    ),
    Font(
        R.font.manrope,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        R.font.manrope,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.manrope,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.manrope,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.manrope,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

@OptIn(ExperimentalTextApi::class)
val FrauncesFontFamily = FontFamily(
    Font(
        R.font.fraunces,
        weight = FontWeight.ExtraLight,
        variationSettings = FontVariation.Settings(FontVariation.weight(200))
    ),
    Font(
        R.font.fraunces,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        R.font.fraunces,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.fraunces,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.fraunces,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.fraunces,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

@Immutable
data class EmberTypography(
    val timerDigits: TextStyle,
    val timerColon: TextStyle,
    val taskTitle: TextStyle,
    val subtitle: TextStyle,
    val topLabel: TextStyle,
    val statusLine: TextStyle,
    val button: TextStyle,
    val body: TextStyle,
    val listCardTitle: TextStyle,
    val caption: TextStyle,
    val statsNumber: TextStyle,
    val material: Typography
)

fun getEmberTypography(preset: String = "MANROPE"): EmberTypography {
    val isFraunces = preset.equals("FRAUNCES", ignoreCase = true)
    val displayFontFamily = if (isFraunces) FrauncesFontFamily else ManropeFontFamily

    val timerDigits = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = if (isFraunces) FontWeight.Light else FontWeight.ExtraLight,
        fontSize = if (isFraunces) 84.sp else 88.sp,
        lineHeight = if (isFraunces) 84.sp else 88.sp,
        letterSpacing = if (isFraunces) (-1.5).sp else (-2).sp,
        fontFeatureSettings = "tnum",
        color = TextCream
    )

    val timerColon = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = if (isFraunces) 74.sp else 80.sp,
        lineHeight = if (isFraunces) 84.sp else 88.sp,
        letterSpacing = 0.sp,
        color = TextCream.copy(alpha = 0.5f)
    )

    val taskTitle = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp,
        color = TextCream
    )

    val subtitle = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp, // 1.4 line height
        letterSpacing = 0.sp,
        color = TextMuted
    )

    val topLabel = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.5.sp,
        color = AmberAccent
    )

    val statusLine = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        color = TextMuted
    )

    val button = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        color = TextCream
    )

    val body = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = TextCream
    )

    val listCardTitle = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp,
        color = TextCream
    )

    val caption = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
        color = TextMuted
    )

    val statsNumber = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1).sp,
        fontFeatureSettings = "tnum",
        color = TextCream
    )

    val materialTypography = Typography(
        displayLarge = timerDigits,
        displayMedium = statsNumber,
        displaySmall = statsNumber.copy(fontSize = 36.sp, lineHeight = 40.sp),
        headlineLarge = taskTitle,
        headlineMedium = taskTitle.copy(fontSize = 24.sp, lineHeight = 30.sp),
        headlineSmall = listCardTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
        titleLarge = listCardTitle.copy(fontSize = 18.sp, lineHeight = 24.sp),
        titleMedium = listCardTitle,
        titleSmall = listCardTitle.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = body.copy(fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = body,
        bodySmall = caption,
        labelLarge = button.copy(fontSize = 14.sp),
        labelMedium = topLabel,
        labelSmall = caption.copy(fontSize = 11.sp, color = AmberAccent)
    )

    return EmberTypography(
        timerDigits = timerDigits,
        timerColon = timerColon,
        taskTitle = taskTitle,
        subtitle = subtitle,
        topLabel = topLabel,
        statusLine = statusLine,
        button = button,
        body = body,
        listCardTitle = listCardTitle,
        caption = caption,
        statsNumber = statsNumber,
        material = materialTypography
    )
}

val LocalEmberTypography = staticCompositionLocalOf { getEmberTypography("MANROPE") }

object EmberTheme {
    val typography: EmberTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalEmberTypography.current
}

val Typography: Typography
    get() = getEmberTypography("MANROPE").material

@Preview(name = "Typography Weights Preview", showBackground = true, backgroundColor = 0xFF0A0705)
@Composable
fun TypographyWeightsPreview() {
    val typo = getEmberTypography("MANROPE")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundTop)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "EMBER TYPOGRAPHY SYSTEM", style = typo.topLabel)
        HorizontalDivider(color = SurfaceBorder)

        Text(text = "24:59", style = typo.timerDigits)
        Text(text = "Focus · ends at 11:20 pm", style = typo.statusLine)

        HorizontalDivider(color = SurfaceBorder)

        Text(text = "ExtraLight 200 - Variable Axis Check", fontFamily = ManropeFontFamily, fontWeight = FontWeight.ExtraLight, fontSize = 16.sp, color = TextCream)
        Text(text = "Light 300 - Variable Axis Check", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Light, fontSize = 16.sp, color = TextCream)
        Text(text = "Normal 400 - Variable Axis Check", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, color = TextCream)
        Text(text = "Medium 500 - Variable Axis Check", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = TextCream)
        Text(text = "SemiBold 600 - Variable Axis Check", fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextCream)
        Text(text = "Bold 700 - Variable Axis Check", fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextCream)

        HorizontalDivider(color = SurfaceBorder)

        Text(text = "Deep Work Session", style = typo.taskTitle)
        Text(text = "Finish authentication architecture · session 2 of 4", style = typo.subtitle)
        Text(text = "3h 45m", style = typo.statsNumber)
        Text(text = "Begin deep focus on selected tasks today.", style = typo.body)
        Text(text = "Card title sample item", style = typo.listCardTitle)
        Text(text = "Updated 10m ago", style = typo.caption)
    }
}
