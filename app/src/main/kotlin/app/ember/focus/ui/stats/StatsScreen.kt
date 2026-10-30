package app.ember.focus.ui.stats

import androidx.core.graphics.toColorInt
import java.util.Locale

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.ember.focus.ui.common.BottomFadeScrim
import app.ember.focus.ui.common.DurationText
import app.ember.focus.ui.common.EmberChip
import app.ember.focus.ui.common.StreakText
import app.ember.focus.ui.common.TopFadeScrim
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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
                .verticalScroll(rememberScrollState())
                .padding(
                    top = topInset + 16.dp,
                    bottom = bottomInset + 24.dp
                )
        ) {
            // TOP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
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
                        contentDescription = "Back",
                        tint = TextCream
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "EMBER · STATS",
                        style = EmberTheme.typography.eyebrow,
                        color = AmberAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Focus Trends",
                        style = EmberTheme.typography.taskTitle,
                        color = TextCream
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB SELECTOR (Week | Month via EmberChip)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EmberChip(
                    text = "This Week",
                    selected = uiState.selectedTab == StatsTab.WEEK,
                    onClick = { viewModel.selectTab(StatsTab.WEEK) },
                    modifier = Modifier.weight(1f)
                )
                EmberChip(
                    text = "This Month",
                    selected = uiState.selectedTab == StatsTab.MONTH,
                    onClick = { viewModel.selectTab(StatsTab.MONTH) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BIG TOTAL BANNER (ST4 & G2: DurationText)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                DurationText(
                    minutes = uiState.rangeTotalMinutes,
                    numberStyle = EmberTheme.typography.heroTotalNumber,
                    unitStyle = EmberTheme.typography.heroTotalUnit
                )

                Spacer(modifier = Modifier.height(4.dp))

                val avgHours = uiState.rangeDailyAverageMinutes / 60
                val avgMins = uiState.rangeDailyAverageMinutes % 60
                val avgStr = if (avgHours > 0) "${avgHours}h ${avgMins}m" else "${avgMins}m"
                val bestDayStr = uiState.bestDayDescription ?: "—"

                Text(
                    text = "Daily average: $avgStr · Best day: $bestDayStr",
                    style = EmberTheme.typography.subtitle,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CHART CONTAINER CARD (ST1..ST3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SecondaryPillBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    if (uiState.selectedTab == StatsTab.WEEK) {
                        WeekBarChart(
                            dayStats = uiState.weekDayStats,
                            dailyGoalMinutes = uiState.dailyGoalMinutes,
                            onDaySelected = { stat -> viewModel.inspectDay(stat) }
                        )
                    } else {
                        Column {
                            MonthHeatmap(
                                daysInMonth = uiState.monthDayStats,
                                dailyGoalMinutes = uiState.dailyGoalMinutes,
                                onDayTapped = { stat -> viewModel.inspectDay(stat) }
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Less", style = EmberTheme.typography.caption, color = TextMuted)
                                Spacer(modifier = Modifier.width(6.dp))
                                listOf(0.2f, 0.45f, 0.75f, 1.0f).forEach { alpha ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(AmberAccent.copy(alpha = alpha))
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Goal", style = EmberTheme.typography.caption, color = AmberAccent)
                            }
                        }
                    }

                    // Inspected Day Readout
                    AnimatedVisibility(
                        visible = uiState.inspectedDay != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.inspectedDay?.let { day ->
                            val h = day.minutes / 60
                            val m = day.minutes % 60
                            val dayStr = day.date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                            val timeStr = if (h > 0) "${h}h ${m}m" else "${m}m"
                            val goalMet = day.minutes >= uiState.dailyGoalMinutes

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SecondaryPillFill)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dayStr,
                                        style = EmberTheme.typography.caption,
                                        color = TextCream
                                    )
                                    Text(
                                        text = "$timeStr ${if (goalMet) "· Goal met" else ""}",
                                        style = EmberTheme.typography.caption,
                                        color = AmberAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // STREAK SUMMARY CARD (G2 big-number/small-unit)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StreakText(
                                days = uiState.currentStreak,
                                numberStyle = EmberTheme.typography.statCardValue,
                                unitStyle = EmberTheme.typography.statCardUnit
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current Streak",
                            style = EmberTheme.typography.caption,
                            color = TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(SecondaryPillBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFFB74D),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StreakText(
                                days = uiState.bestStreak,
                                numberStyle = EmberTheme.typography.statCardValue,
                                unitStyle = EmberTheme.typography.statCardUnit
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Best Streak",
                            style = EmberTheme.typography.caption,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CATEGORY BREAKDOWN
            if (uiState.categoryShares.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "CATEGORY BREAKDOWN",
                            style = EmberTheme.typography.eyebrow,
                            color = AmberAccent
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        uiState.categoryShares.forEach { share ->
                            val catColor = try {
                                Color(share.colorHex.toColorInt())
                            } catch (_: Exception) {
                                AmberAccent
                            }

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = share.categoryName,
                                            style = EmberTheme.typography.body,
                                            color = TextCream
                                        )
                                    }

                                    val h = share.totalMinutes / 60
                                    val m = share.totalMinutes % 60
                                    val timeStr = if (h > 0) "${h}h ${m}m" else "${m}m"
                                    val pctStr = "${(share.percentage * 100).toInt()}%"

                                    Text(
                                        text = "$timeStr ($pctStr)",
                                        style = EmberTheme.typography.caption,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { share.percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = catColor,
                                    trackColor = SecondaryPillFill
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // PERFORMANCE METRICS GRID (ST3 & ST4: 3 equal-height cards)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quality Rating Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(108.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            val ratingStr = if (uiState.averageRating != null) {
                                String.format(Locale.US, "%.1f / 5", uiState.averageRating)
                            } else "—"

                            Text(
                                text = ratingStr,
                                style = EmberTheme.typography.statCardValue,
                                color = TextCream
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Avg Quality",
                                style = EmberTheme.typography.caption,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Completion Rate Card (ST3: "—" when zero sessions)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(108.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            val compStr = if (uiState.completionRatePercentage != null) {
                                "${uiState.completionRatePercentage}%"
                            } else "—"

                            Text(
                                text = compStr,
                                style = EmberTheme.typography.statCardValue,
                                color = TextCream
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Completion",
                                style = EmberTheme.typography.caption,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Longest Session Card (ST3: "—" when zero sessions)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(108.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            if (uiState.longestSessionMinutes != null) {
                                DurationText(
                                    minutes = uiState.longestSessionMinutes!!,
                                    numberStyle = EmberTheme.typography.statCardValue,
                                    unitStyle = EmberTheme.typography.statCardUnit
                                )
                            } else {
                                Text(
                                    text = "—",
                                    style = EmberTheme.typography.statCardValue,
                                    color = TextCream
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Longest",
                                style = EmberTheme.typography.caption,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Vertical Scrims under status bar and above nav bar (G3)
        TopFadeScrim()
        BottomFadeScrim()
    }
}
