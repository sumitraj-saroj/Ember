package app.ember.focus.ui.focus

import java.util.Locale

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SecondaryPillText
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import app.ember.focus.ui.theme.Typography
import kotlinx.coroutines.delay

@Composable
fun BreakScreen(
    durationMinutes: Int = 5,
    onSkipBreak: () -> Unit
) {
    var remainingSeconds by remember { mutableIntStateOf(durationMinutes * 60) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        onSkipBreak()
    }

    val totalSec = durationMinutes * 60
    val progress = remainingSeconds.toFloat() / totalSec.toFloat()

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = "TAKE A BREATH",
                style = Typography.labelMedium,
                color = AmberAccent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (durationMinutes > 5) "Extended Break" else "Take Five",
                style = Typography.headlineMedium,
                color = TextCream
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rest your eyes, hydrate, and stretch",
                style = Typography.bodyMedium,
                color = TextMuted
            )
        }

        // Center calm circular progress countdown
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Outer breathing glow
                drawCircle(
                    color = AmberAccent.copy(alpha = breatheAlpha * 0.2f),
                    radius = size.minDimension * 0.48f
                )

                // Track
                drawCircle(
                    color = SecondaryPillFill,
                    radius = size.minDimension * 0.42f,
                    style = Stroke(width = 6.dp.toPx())
                )

                // Progress Arc
                drawArc(
                    color = AmberAccent,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val m = remainingSeconds / 60
                val s = remainingSeconds % 60
                Text(
                    text = String.format(Locale.US, "%02d:%02d", m, s),
                    style = Typography.displayMedium,
                    color = TextCream
                )
                Text(
                    text = "resting",
                    style = Typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        // Bottom Skip Button
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(24.dp))
                .background(SecondaryPillFill)
                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(24.dp))
                .clickable { onSkipBreak() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Skip break",
                style = Typography.labelLarge,
                color = SecondaryPillText
            )
        }
    }
}
