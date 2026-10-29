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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import app.ember.focus.ui.theme.AmberAccent
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
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            .verticalScroll(rememberScrollState())
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SecondaryPillFill)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextCream
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "EMBER STATS",
                    style = Typography.labelSmall,
                    color = AmberAccent
                )
                Text(
                    text = "Focus Trends",
                    style = Typography.titleLarge,
                    color = TextCream
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TAB SELECTOR (Week | Month)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsTab.entries.forEach { tab ->
                val isSelected = uiState.selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrimaryPillFill else SecondaryPillFill)
                        .border(1.dp, if (isSelected) PrimaryPillFill else SecondaryPillBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.selectTab(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tab == StatsTab.WEEK) "This Week" else "This Month",
                        style = Typography.labelLarge,
                        color = if (isSelected) PrimaryPillText else SecondaryPillText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BIG TOTAL BANNER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            val totalHours = uiState.rangeTotalMinutes / 60
            val totalMins = uiState.rangeTotalMinutes % 60
            val totalStr = if (totalHours > 0) "${totalHours}h ${totalMins}m" else "${totalMins}m"

            Text(
                text = totalStr,
                style = Typography.displayLarge,
                color = TextCream
            )

            val avgHours = uiState.rangeDailyAverageMinutes / 60
            val avgMins = uiState.rangeDailyAverageMinutes % 60
            val avgStr = if (avgHours > 0) "${avgHours}h ${avgMins}m" else "${avgMins}m"

            Text(
                text = "Daily average: $avgStr · Best day: ${uiState.bestDayDescription}",
                style = Typography.bodyMedium,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CHART CONTAINER CARD
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
                    MonthHeatmap(
                        daysInMonth = uiState.monthDayStats,
                        dailyGoalMinutes = uiState.dailyGoalMinutes,
                        onDayTapped = { stat -> viewModel.inspectDay(stat) }
                    )
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
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dayStr,
                                    fontSize = 12.sp,
                                    color = TextCream
                                )
                                Text(
                                    text = "$timeStr ${if (goalMet) "· Goal met" else ""}",
                                    fontSize = 12.sp,
                                    color = AmberAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STREAK SUMMARY CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
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
                        Text(
                            text = "${uiState.currentStreak} days",
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextCream
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Current Streak",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
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
                        Text(
                            text = "${uiState.bestStreak} days",
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextCream
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Best Streak",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        style = Typography.labelSmall,
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
                                        style = Typography.bodyMedium,
                                        color = TextCream
                                    )
                                }

                                val h = share.totalMinutes / 60
                                val m = share.totalMinutes % 60
                                val timeStr = if (h > 0) "${h}h ${m}m" else "${m}m"
                                val pctStr = "${(share.percentage * 100).toInt()}%"

                                Text(
                                    text = "$timeStr ($pctStr)",
                                    style = Typography.bodySmall,
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

            Spacer(modifier = Modifier.height(16.dp))
        }

        // PERFORMANCE METRICS GRID
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
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val ratingStr = if (uiState.averageRating > 0f) String.format(Locale.US, "%.1f", uiState.averageRating) else "-"
                    Text(
                        text = "$ratingStr / 5",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextCream
                    )
                    Text(
                        text = "Avg Quality",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Completion Rate Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${uiState.completionRatePercentage}%",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextCream
                    )
                    Text(
                        text = "Completion",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Longest Session Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val h = uiState.longestSessionMinutes / 60
                    val m = uiState.longestSessionMinutes % 60
                    val longestStr = if (h > 0) "${h}h ${m}m" else "${m}m"
                    Text(
                        text = longestStr,
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextCream
                    )
                    Text(
                        text = "Longest",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
