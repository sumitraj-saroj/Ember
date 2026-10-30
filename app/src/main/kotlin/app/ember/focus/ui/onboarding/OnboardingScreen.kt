package app.ember.focus.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.ui.candle.CandlePhase
import app.ember.focus.ui.candle.CandleState
import app.ember.focus.ui.candle.CandleView
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.PrimaryPillFill
import app.ember.focus.ui.theme.PrimaryPillText
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    settingsRepository: SettingsRepository,
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    var dailyGoalHours by remember { mutableFloatStateOf(4f) }
    val scope = rememberCoroutineScope()

    val completeOnboarding: () -> Unit = {
        scope.launch {
            settingsRepository.updateDailyGoalMinutes((dailyGoalHours * 60).toInt())
            settingsRepository.setOnboardingCompleted(true)
            onFinishOnboarding()
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
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding with Skip action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EMBER",
                    style = EmberTheme.typography.topLabel,
                    color = AmberAccent
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = completeOnboarding)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Skip",
                        style = EmberTheme.typography.button.copy(fontSize = 14.sp),
                        color = AmberAccent
                    )
                }
            }

            // Swipeable Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> {
                            // Page 1: Candle concept
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CandleView(
                                    state = CandleState(
                                        progress = 0.25f,
                                        phase = CandlePhase.BURNING
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Focus as a Burning Candle",
                                style = EmberTheme.typography.taskTitle,
                                color = TextCream,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Every deep work session is a real candle that melts away. Watch your focus take physical form and single-task with intention.",
                                style = EmberTheme.typography.body,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }

                        1 -> {
                            // Page 2: Strict mode
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryPillFill),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Strict Discipline",
                                style = EmberTheme.typography.taskTitle,
                                color = TextCream,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Leaving Ember for another app blows out your candle and marks the session broken. Locking your phone is always safe.",
                                style = EmberTheme.typography.body,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }

                        2 -> {
                            // Page 3: Daily goal setup
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Set Your Ambition",
                                style = EmberTheme.typography.taskTitle,
                                color = TextCream,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose your daily focus target to ignite your streak.",
                                style = EmberTheme.typography.body,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = "${dailyGoalHours.toInt()} hours / day",
                                style = EmberTheme.typography.statsNumber,
                                color = AmberAccent
                            )

                            Slider(
                                value = dailyGoalHours,
                                onValueChange = { dailyGoalHours = it },
                                valueRange = 1f..8f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = AmberAccent,
                                    activeTrackColor = AmberAccent,
                                    inactiveTrackColor = SecondaryPillFill
                                ),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            )
                        }
                    }
                }
            }

            // Bottom navigation: Indicator dots and Pill button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicator dots (clickable)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    for (i in 0..2) {
                        val isSelected = i == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AmberAccent else TextMuted.copy(alpha = 0.35f))
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                        )
                    }
                }

                // Action Pill — whole Box is clickable with large touch target
                val buttonText = when (pagerState.currentPage) {
                    0 -> "Next: Strict Discipline →"
                    1 -> "Next: Set Daily Goal →"
                    else -> "Light Your First Candle"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(PrimaryPillFill)
                        .clickable {
                            val current = pagerState.currentPage
                            if (current < 2) {
                                scope.launch {
                                    pagerState.animateScrollToPage(current + 1)
                                }
                            } else {
                                completeOnboarding()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        style = EmberTheme.typography.button,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPillText
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
