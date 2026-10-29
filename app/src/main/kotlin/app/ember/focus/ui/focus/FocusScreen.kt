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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import app.ember.focus.ui.theme.PrimaryPillFill
import app.ember.focus.ui.theme.PrimaryPillText
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SecondaryPillText
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import app.ember.focus.ui.theme.Typography

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
    val view = LocalView.current

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

    // Immersive mode during session
    DisposableEffect(timerState.status) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (timerState.status == TimerStatus.RUNNING || timerState.status == TimerStatus.OVERTIME) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val window = (context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SecondaryPillFill)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return to day view",
                        tint = TextCream
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "EMBER",
                        style = Typography.labelMedium,
                        color = AmberAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timerState.taskName.ifBlank { "Deep work" },
                        style = Typography.titleLarge,
                        color = TextCream,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            style = Typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleAmbientSound() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SecondaryPillFill)
                ) {
                    Icon(
                        imageVector = if (settings.ambientSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = if (settings.ambientSoundEnabled) "Mute fire crackle" else "Enable fire crackle",
                        tint = if (settings.ambientSoundEnabled) AmberAccent else TextMuted
                    )
                }
            }

            // Strict mode grace timer banner
            AnimatedVisibility(
                visible = timerState.isGraceTimerRunning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF552211))
                        .border(1.dp, Color(0xFFFF5722), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Grace timer: ${timerState.graceRemainingSec}s before candle blows out!",
                        color = Color(0xFFFFCCBC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // CENTER CANDLE VIEW
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
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

            // TIMER DIGITS & STATUS LINE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                val isOvertime = timerState.status == TimerStatus.OVERTIME
                val displaySec = if (isOvertime) timerState.overtimeSec else timerState.remainingSec

                RollingTimerDisplay(
                    seconds = displaySec,
                    isOvertime = isOvertime
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                    style = Typography.bodyMedium,
                    color = if (timerState.status == TimerStatus.BROKEN) AshGray else TextMuted
                )
            }

            // BOTTOM ACTION BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (timerState.status) {
                    TimerStatus.RUNNING, TimerStatus.PAUSED -> {
                        // Left: Pause/Resume (Dark pill)
                        val isPaused = timerState.status == TimerStatus.PAUSED
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(SecondaryPillFill)
                                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(32.dp))
                                .clickable { viewModel.onPauseResumeClicked() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPaused) "Resume" else "Pause",
                                style = Typography.labelLarge,
                                color = SecondaryPillText
                            )
                        }

                        // Right: Reset (Cream pill)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(PrimaryPillFill)
                                .clickable { viewModel.onResetClicked() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Reset",
                                style = Typography.labelLarge,
                                color = PrimaryPillText
                            )
                        }
                    }

                    TimerStatus.OVERTIME -> {
                        // Left: Pause
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(SecondaryPillFill)
                                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(32.dp))
                                .clickable { viewModel.onPauseResumeClicked() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pause",
                                style = Typography.labelLarge,
                                color = SecondaryPillText
                            )
                        }

                        // Right: Finish
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(PrimaryPillFill)
                                .clickable { viewModel.onFinishClicked() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Finish",
                                style = Typography.labelLarge,
                                color = PrimaryPillText
                            )
                        }
                    }

                    TimerStatus.COMPLETED, TimerStatus.BROKEN, TimerStatus.IDLE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(32.dp))
                                .background(PrimaryPillFill)
                                .clickable { onNavigateBack() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Done",
                                style = Typography.labelLarge,
                                color = PrimaryPillText
                            )
                        }
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
                    style = Typography.titleLarge,
                    color = TextCream
                )
            },
            text = {
                Text(
                    text = msg,
                    style = Typography.bodyMedium,
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
                    Text("Reset candle", color = AmberAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetDialog() }) {
                    Text("Keep burning", color = TextMuted)
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
