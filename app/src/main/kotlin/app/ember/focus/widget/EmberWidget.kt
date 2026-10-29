package app.ember.focus.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import app.ember.focus.MainActivity
import app.ember.focus.data.local.dao.SessionDao
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.domain.util.calculateStreaks
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class EmberWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun sessionDao(): SessionDao
        fun settingsRepository(): SettingsRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val sessionDao = entryPoint.sessionDao()
        val settingsRepo = entryPoint.settingsRepository()

        val settings = settingsRepo.settingsFlow.first()
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val startEpoch = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val todaySessions = sessionDao.getSessionsBetween(startEpoch, endEpoch).first()
        val allSessions = sessionDao.getAllSessions().first()

        val todayMinutes = todaySessions.sumOf { it.focusedSec } / 60
        val dailyGoalMinutes = settings.dailyGoalMinutes

        val dayMinutesMap = allSessions.groupBy {
            Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate()
        }.mapValues { entry ->
            entry.value.sumOf { it.focusedSec } / 60
        }

        val (currentStreak, _) = calculateStreaks(dayMinutesMap, today, dailyGoalMinutes)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    todayMinutes = todayMinutes,
                    dailyGoalMinutes = dailyGoalMinutes,
                    currentStreak = currentStreak
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun WidgetContent(
    todayMinutes: Int,
    dailyGoalMinutes: Int,
    currentStreak: Int
) {
    val h = todayMinutes / 60
    val m = todayMinutes % 60
    val totalStr = if (h > 0) "${h}h ${m}m" else "${m}m"
    val goalHours = dailyGoalMinutes / 60
    val pct = if (dailyGoalMinutes > 0) ((todayMinutes.toFloat() / dailyGoalMinutes.toFloat()) * 100).toInt() else 0

    val amberColor = androidx.compose.ui.graphics.Color(0xFFE8A15A)
    val creamColor = androidx.compose.ui.graphics.Color(0xFFF2E6D0)
    val mutedColor = androidx.compose.ui.graphics.Color(0xFF8C8176)
    val bgColor = androidx.compose.ui.graphics.Color(0xFF171310)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bgColor, bgColor))
            .cornerRadius(24.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "EMBER",
                    style = TextStyle(
                        color = ColorProvider(amberColor, amberColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "$currentStreak🔥",
                    style = TextStyle(
                        color = ColorProvider(creamColor, creamColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            Text(
                text = totalStr,
                style = TextStyle(
                    color = ColorProvider(creamColor, creamColor),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = "of ${goalHours}h goal · $pct%",
                style = TextStyle(
                    color = ColorProvider(mutedColor, mutedColor),
                    fontSize = 12.sp
                )
            )
        }
    }
}

class EmberWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EmberWidget()
}
