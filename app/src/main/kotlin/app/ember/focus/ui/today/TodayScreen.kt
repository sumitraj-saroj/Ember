package app.ember.focus.ui.today

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import app.ember.focus.ui.common.BottomFadeScrim
import app.ember.focus.ui.common.DurationText
import app.ember.focus.ui.common.TopFadeScrim
import app.ember.focus.ui.theme.TrackInactive
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.core.graphics.toColorInt
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.ember.focus.BuildConfig
import app.ember.focus.core.timer.TimerStatus
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.service.TimerService
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
import app.ember.focus.ui.theme.Typography
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToStartSession: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCandleLab: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val recentTasks by viewModel.recentTasks.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedDetailSession by viewModel.selectedDetailSession.collectAsState()

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val zoneId = remember { ZoneId.systemDefault() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }

    val isToday = uiState.selectedDate == LocalDate.now()
    val isYesterday = uiState.selectedDate == LocalDate.now().minusDays(1)
    val dateLabel = when {
        isToday -> "Today"
        isYesterday -> "Yesterday"
        else -> uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    }

    val goalProgress = if (uiState.dailyGoalMinutes > 0) {
        (uiState.totalFocusedMinutes.toFloat() / uiState.dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = goalProgress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "goalProgress"
    )

    // Scroll to highlighted session when tapped on timeline
    LaunchedEffect(uiState.highlightedSessionId) {
        val id = uiState.highlightedSessionId
        if (id != null) {
            val index = uiState.sessions.indexOfFirst { it.id == id }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarHeight + 8.dp)
        ) {
            // T1: TOP BAR: Branding on left (tracked Eyebrow), Quick Actions on right (44dp circles, spaced by 8dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EMBER",
                        style = EmberTheme.typography.eyebrow
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToStats,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SecondaryPillFill)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Stats",
                            tint = TextCream,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (BuildConfig.DEBUG) {
                        IconButton(
                            onClick = onNavigateToCandleLab,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SecondaryPillFill)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Candle Lab",
                                tint = AmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SecondaryPillFill)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextCream,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // T2: DATE SELECTOR PILL (Centered, 40dp arrow targets, 30% alpha forward arrow when disabled)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SecondaryPillFill)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.prevDay() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous day",
                            tint = TextCream,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val d = uiState.selectedDate
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        viewModel.selectDate(LocalDate.of(year, month + 1, dayOfMonth))
                                    },
                                    d.year,
                                    d.monthValue - 1,
                                    d.dayOfMonth
                                ).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateLabel,
                            style = EmberTheme.typography.button.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            color = TextCream
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick date",
                            tint = AmberAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextDay() },
                        enabled = !isToday,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next day",
                            tint = if (isToday) TextCream.copy(alpha = 0.3f) else TextCream,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // MINI BURNING CANDLE BANNER (If session active)
            val isSessionActive = timerState.status == TimerStatus.RUNNING ||
                    timerState.status == TimerStatus.PAUSED ||
                    timerState.status == TimerStatus.OVERTIME

            AnimatedVisibility(visible = isSessionActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF261D16))
                        .border(1.dp, AmberAccent.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .clickable { onNavigateToFocus() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = timerState.taskName.ifBlank { "Deep work" },
                                    style = EmberTheme.typography.listCardTitle,
                                    color = TextCream
                                )
                                val remaining = if (timerState.status == TimerStatus.OVERTIME) {
                                    "+${timerState.overtimeSec / 60}m overtime"
                                } else {
                                    "${timerState.remainingSec / 60}m remaining"
                                }
                                Text(
                                    text = "${timerState.status.name.lowercase().replaceFirstChar { it.uppercase() }} · $remaining",
                                    style = EmberTheme.typography.caption,
                                    color = AmberAccent
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(AmberAccent)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Return",
                                style = EmberTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPillText
                            )
                        }
                    }
                }
            }

            // T3: TOTAL TIME & GOAL PROGRESS BANNER (DurationText + Streak chip without tracking)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        DurationText(minutes = uiState.totalFocusedMinutes)
                        Spacer(modifier = Modifier.height(2.dp))
                        val goalHours = uiState.dailyGoalMinutes / 60
                        Text(
                            text = "of ${goalHours}h goal",
                            style = EmberTheme.typography.body.copy(fontSize = 15.sp, color = TextMuted)
                        )
                    }

                    // Streak Badge (G1: No letter spacing)
                    val streakText = when (uiState.currentStreak) {
                        0 -> "No streak yet"
                        1 -> "1 day streak"
                        else -> "${uiState.currentStreak} day streak"
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecondaryPillFill)
                            .border(1.dp, SecondaryPillBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = streakText,
                            style = EmberTheme.typography.button.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                            color = TextCream
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // T4: Progress Bar 8dp tall, rounded, track #2A241F, amber fill + faint glow, checkmark icon at completion, no end dot
                val isGoalReached = animatedProgress >= 1f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TrackInactive)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isGoalReached) {
                                        listOf(AmberAccent, Color(0xFFFFD59E))
                                    } else {
                                        listOf(AmberAccent.copy(alpha = 0.85f), AmberAccent)
                                    }
                                )
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (isGoalReached) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF1A120C),
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(end = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // T5: Completed vs Broken on left, % of goal on right (both 13sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.completedCount} completed · ${uiState.brokenCount} broken",
                        style = EmberTheme.typography.caption.copy(fontSize = 13.sp, color = TextMuted)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isGoalReached) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Goal reached",
                                tint = AmberAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${(goalProgress * 100).toInt()}% of goal",
                            style = EmberTheme.typography.caption.copy(fontSize = 13.sp, color = AmberAccent)
                        )
                    }
                }
            }

            // HORIZONTAL DAY TIMELINE
            if (uiState.sessions.isNotEmpty()) {
                TodayTimeline(
                    sessions = uiState.sessions,
                    selectedDate = uiState.selectedDate,
                    highlightedSessionId = uiState.highlightedSessionId,
                    onSessionTapped = { s ->
                        viewModel.highlightSession(s.id)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // SESSION LIST OR EMPTY STATE
            if (uiState.sessions.isEmpty()) {
                // T6: Empty state with unlit candle (flexible weight 1f, ~40% screen height, 20sp SemiBold title, 15sp subtitle, 24dp gap, full-width CTA)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f),
                        contentAlignment = Alignment.Center
                    ) {
                        CandleView(
                            state = CandleState(
                                progress = 0f,
                                phase = CandlePhase.IDLE
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No candles lit yet",
                        style = EmberTheme.typography.taskTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                        color = TextCream
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Light your first candle of the day.",
                        style = EmberTheme.typography.body.copy(fontSize = 15.sp, color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(PrimaryPillFill)
                            .clickable { onNavigateToStartSession() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = PrimaryPillText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Light a candle",
                                style = EmberTheme.typography.button,
                                color = PrimaryPillText
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.sessions,
                        key = { it.id }
                    ) { session ->
                        val isHighlighted = session.id == uiState.highlightedSessionId
                        SessionCard(
                            session = session,
                            isHighlighted = isHighlighted,
                            onClick = { viewModel.openDetailSheet(session) }
                        )
                    }
                }
            }
        }

        // T6: When sessions exist, button pinned at the bottom with a fade behind it
        if (uiState.sessions.isNotEmpty()) {
            BottomFadeScrim(
                modifier = Modifier.align(Alignment.BottomCenter),
                extraHeight = 72.dp
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(PrimaryPillFill)
                        .clickable { onNavigateToStartSession() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = PrimaryPillText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Light a candle",
                            style = EmberTheme.typography.button,
                            color = PrimaryPillText
                        )
                    }
                }
            }
        }

        // G3: Top fade scrim for smooth scroll under transparent status bar
        TopFadeScrim(modifier = Modifier.align(Alignment.TopCenter))
    }

    // Session Detail / Edit Sheet
    selectedDetailSession?.let { session ->
        SessionDetailSheet(
            session = session,
            categories = categories,
            onDismiss = { viewModel.dismissDetailSheet() },
            onUpdateSession = { updated -> viewModel.updateSession(updated) },
            onDeleteSession = { id -> viewModel.deleteSession(id) }
        )
    }
}

@Composable
fun SessionCard(
    session: Session,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val startStr = LocalDateTime.ofInstant(Instant.ofEpochMilli(session.startedAt), zoneId).format(timeFormatter).lowercase()
    val endStr = LocalDateTime.ofInstant(Instant.ofEpochMilli(session.endedAt), zoneId).format(timeFormatter).lowercase()

    val catColor = try {
        Color(session.categoryColorHex.toColorInt())
    } catch (_: Exception) {
        AmberAccent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(
                width = if (isHighlighted) 2.dp else 1.dp,
                color = if (isHighlighted) AmberAccent else SecondaryPillBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Range
                Text(
                    text = "$startStr - $endStr",
                    style = Typography.bodySmall,
                    color = TextMuted
                )

                // Category Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(catColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = session.categoryName,
                        style = EmberTheme.typography.caption,
                        color = catColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task Name
            Text(
                text = session.taskName.ifBlank { "Deep work" },
                style = Typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextCream
            )

            // Goal or Note if present
            if (!session.goal.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Goal: ${session.goal}",
                    style = Typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration & Rating / Broken Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val focusedMins = session.focusedSec / 60
                val durationText = if (session.overtimeSec > 0) {
                    val overtimeMins = session.overtimeSec / 60
                    "${focusedMins} min (+${overtimeMins}m)"
                } else {
                    "${focusedMins} min"
                }

                Text(
                    text = durationText,
                    style = Typography.labelMedium,
                    color = AmberAccent
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rating embers
                    if (session.rating != null && session.rating > 0) {
                        for (i in 1..session.rating) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (session.status == SessionStatus.BROKEN) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AshGray.copy(alpha = 0.2f))
                                .border(1.dp, AshGray, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Broken",
                                style = EmberTheme.typography.caption,
                                color = AshGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
