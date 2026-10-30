package app.ember.focus.ui.focus

import java.util.Locale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.TextCream

@Composable
fun RollingTimerDisplay(
    seconds: Int,
    isOvertime: Boolean,
    modifier: Modifier = Modifier,
    color: Color = TextCream
) {
    val totalSec = seconds.coerceAtLeast(0)
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val secs = totalSec % 60

    val timeString = buildString {
        if (isOvertime) append("+")
        if (hours > 0) {
            append(hours)
            append(":")
            append(String.format(Locale.US, "%02d", minutes))
            append(":")
            append(String.format(Locale.US, "%02d", secs))
        } else {
            append(String.format(Locale.US, "%02d", minutes))
            append(":")
            append(String.format(Locale.US, "%02d", secs))
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        timeString.forEachIndexed { index, char ->
            if (char.isDigit()) {
                RollingDigit(
                    digit = char,
                    color = color
                )
            } else if (char == ':') {
                ColonSeparator(color = color)
            } else {
                PlusPrefix(color = color)
            }
        }
    }
}

@Composable
fun RollingDigit(
    digit: Char,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Fixed width slot to guarantee 0 width-jitter when digits change
    Box(
        modifier = modifier
            .width(52.dp)
            .height(96.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (slideInVertically(
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) { height -> height } + fadeIn(tween(200))) togetherWith
                (slideOutVertically(
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) { height -> -height } + fadeOut(tween(200)))
            },
            label = "rollingDigit"
        ) { targetDigit ->
            Text(
                text = targetDigit.toString(),
                style = EmberTheme.typography.timerDigits.copy(color = color),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ColonSeparator(
    color: Color,
    modifier: Modifier = Modifier
) {
    // Colon drawn separately, vertically centered, 50% opacity
    Box(
        modifier = modifier
            .width(22.dp)
            .height(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ":",
            style = EmberTheme.typography.timerColon.copy(
                color = color.copy(alpha = 0.5f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlusPrefix(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(36.dp)
            .height(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = EmberTheme.typography.timerDigits.copy(
                fontSize = 52.sp,
                lineHeight = 56.sp,
                color = color
            ),
            textAlign = TextAlign.Center
        )
    }
}
