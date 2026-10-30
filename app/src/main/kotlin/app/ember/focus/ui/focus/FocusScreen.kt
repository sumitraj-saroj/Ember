package app.ember.focus.ui.focus

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.ember.focus.core.timer.TimerStatus
import app.ember.focus.domain.util.formatSessionPlanLabel
import app.ember.focus.ui.candle.CandlePhase
import app.ember.focus.ui.candle.CandleState
import app.ember.focus.ui.candle.CandleView
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.AshGray
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.PrimaryPillFill
import app.ember.focus.ui.theme.PrimaryPillText
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SecondaryPillText
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onNavigateBack: () -> Unit,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val showReflection by viewModel.showReflection.collectAsState()
    val showBreak by viewModel.showBreak.collectAsState()
    val showResetDialog by viewModel.showResetDialog.collectAsState()

    val context = LocalContext.current

    // Keep screen on while burning (if setting enabled)
    DisposableEffect(settings.keepScreenOn, timerState.status) {
        val window = (context as? Activity)?.window
        val shouldKeepOn = settings.keepScreenOn &&
                (timerState.status == TimerStatus.RUNNING || timerState.status == TimerStatus.OVERTIME)
        if (shouldKeepOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler {
        onNavigateBack()
    }

    if (showBreak) {
        val breakDuration = if (timerState.completedSessionsCount > 0 && timerState.completedSessionsCount % 4 == 0) {
            settings.longBreakMinutes
        } else {
            settings.shortBreakMinutes
        }
        BreakScreen(
            durationMinutes = breakDuration,
            onSkipBreak = {
                viewModel.dismissBreak()
                onNavigateBack()
            }
        )
        return
    }

    val candlePhase = remember(timerState.status) {
        when (timerState.status) {
            TimerStatus.IDLE -> CandlePhase.IDLE
            TimerStatus.RUNNING -> CandlePhase.BURNING
            TimerStatus.PAUSED -> CandlePhase.PAUSED
            TimerStatus.OVERTIME -> CandlePhase.OVERTIME
            TimerStatus.COMPLETED -> CandlePhase.FINISHED
            TimerStatus.BROKEN -> CandlePhase.BROKEN
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom)
                )
            )
            .safeDrawingPadding() // Ensures status bars, display cutouts (notch) & navigation bars are padded
    ) {
        // 16dp spacing below status bar before header starts
        Spacer(modifier = Modifier.height(16.dp))

        // ZONE 1: TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SecondaryPillFill)
                    .border(1.dp, SecondaryPillBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Return to day view",
                    tint = TextCream,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                // Top label in small caps (app name)
                Text(
                    text = "EMBER",
                    style = EmberTheme.typography.eyebrow,
                    color = AmberAccent,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Task title (28sp, tight)
                Text(
                    text = timerState.taskName.ifBlank { "Deep work" },
                    style = EmberTheme.typography.taskTitle,
                    color = TextCream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                // Subtitle: goal and planned session count
                val planLabel = formatSessionPlanLabel(
                    timerState.completedSessionsCount,
                    timerState.plannedSessionCount
                )
                val subtitle = buildString {
                    if (!timerState.goal.isNullOrBlank()) {
                        append(timerState.goal)
                        if (planLabel != null) append(" · ")
                    }
                    if (planLabel != null) append(planLabel)
                }

                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = EmberTheme.typography.subtitle,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            IconButton(
                onClick = { viewModel.toggleAmbientSound() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SecondaryPillFill)
                    .border(1.dp, SecondaryPillBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (settings.ambientSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = if (settings.ambientSoundEnabled) "Mute fire crackle" else "Enable fire crackle",
                    tint = if (settings.ambientSoundEnabled) AmberAccent else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Strict mode grace timer banner (if running)
        AnimatedVisibility(
            visible = timerState.isGraceTimerRunning,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF552211))
                    .border(1.dp, Color(0xFFFF5722), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Grace timer: ${timerState.graceRemainingSec}s before candle blows out!",
                    style = EmberTheme.typography.caption,
                    color = Color(0xFFFFCCBC),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Vertical rhythm: header to candle gap about 24dp
        Spacer(modifier = Modifier.height(24.dp))

        // ZONE 2: FLEXIBLE AREA (weight 1f)
        // Candle sits centered and scales to available height, with timer and status directly below it
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Scalable Candle View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CandleView(
                    state = CandleState(
                        progress = timerState.progress,
                        phase = candlePhase,
                        seed = timerState.candleSeed
                    )
                )
            }

            // Vertical rhythm: candle to timer gap about 16dp
            Spacer(modifier = Modifier.height(16.dp))

            // Timer digits
            val isOvertime = timerState.status == TimerStatus.OVERTIME
            val displaySec = if (isOvertime) timerState.overtimeSec else timerState.remainingSec

            RollingTimerDisplay(
                seconds = displaySec,
                isOvertime = isOvertime
            )

            // Vertical rhythm: timer to status gap 8dp
            Spacer(modifier = Modifier.height(8.dp))

            // Status line under the timer
            val statusText = when (timerState.status) {
                TimerStatus.RUNNING -> "Focus · ends at ${viewModel.getEstimatedEndTime()}"
                TimerStatus.PAUSED -> "Paused"
                TimerStatus.OVERTIME -> "In flow · +${timerState.overtimeSec / 60} min"
                TimerStatus.COMPLETED -> "Session done · take five"
                TimerStatus.BROKEN -> "Candle went out · ${timerState.totalFocusedMinutes} min counted"
                TimerStatus.IDLE -> "Ready to ignite"
            }

            Text(
                text = statusText,
                style = EmberTheme.typography.statusLine,
                color = if (timerState.status == TimerStatus.BROKEN) AshGray else TextMuted,
                textAlign = TextAlign.Center
            )
        }

        // Gap between flexible area and button row
        Spacer(modifier = Modifier.height(24.dp))

        // ZONE 3: BUTTON ROW AT THE BOTTOM
        // Buttons are 56dp high, 12dp between them, 24dp horizontal screen padding,
        // with 32dp extra bottom margin above navigation bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (timerState.status) {
                TimerStatus.RUNNING, TimerStatus.PAUSED -> {
                    val isPaused = timerState.status == TimerStatus.PAUSED
                    // Left: Pause/Resume (Dark pill)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(SecondaryPillFill)
                            .border(1.dp, SecondaryPillBorder, RoundedCornerShape(28.dp))
                            .clickable { viewModel.onPauseResumeClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPaused) "Resume" else "Pause",
                            style = EmberTheme.typography.button,
                            color = SecondaryPillText
                        )
                    }

                    // Right: Reset (Cream pill)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(PrimaryPillFill)
                            .clickable { viewModel.onResetClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reset",
                            style = EmberTheme.typography.button,
                            color = PrimaryPillText
                        )
                    }
                }

                TimerStatus.OVERTIME -> {
                    // Left: Pause
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(SecondaryPillFill)
                            .border(1.dp, SecondaryPillBorder, RoundedCornerShape(28.dp))
                            .clickable { viewModel.onPauseResumeClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pause",
                            style = EmberTheme.typography.button,
                            color = SecondaryPillText
                        )
                    }

                    // Right: Finish
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(PrimaryPillFill)
                            .clickable { viewModel.onFinishClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Finish",
                            style = EmberTheme.typography.button,
                            color = PrimaryPillText
                        )
                    }
                }

                TimerStatus.COMPLETED, TimerStatus.BROKEN, TimerStatus.IDLE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(PrimaryPillFill)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Done",
                            style = EmberTheme.typography.button,
                            color = PrimaryPillText
                        )
                    }
                }
            }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        val burnedSec = timerState.totalFocusedSec
        val msg = if (burnedSec >= 60) {
            "This candle has burned for ${burnedSec / 60} min. Resetting will mark this session as broken with ${burnedSec / 60} minutes counted, and prepare a fresh candle."
        } else {
            "Less than a minute has burned. This session will be discarded."
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissResetDialog() },
            containerColor = SurfaceCard,
            title = {
                Text(
                    text = "Extinguish candle?",
                    style = EmberTheme.typography.listCardTitle,
                    color = TextCream
                )
            },
            text = {
                Text(
                    text = msg,
                    style = EmberTheme.typography.body,
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val broken = viewModel.confirmReset()
                        if (broken) {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Reset candle", style = EmberTheme.typography.button, color = AmberAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetDialog() }) {
                    Text("Keep burning", style = EmberTheme.typography.button, color = TextMuted)
                }
            }
        )
    }

    // Reflection Bottom Sheet
    if (showReflection) {
        ReflectionSheet(
            goal = timerState.goal,
            onDismiss = { viewModel.dismissReflection() },
            onSaveReflection = { rating, note, goalAchieved ->
                viewModel.saveReflection(rating, note, goalAchieved)
            }
        )
    }
}
