package app.ember.focus.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.ember.focus.MainActivity
import app.ember.focus.R
import app.ember.focus.data.local.dao.SessionDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderWorkerEntryPoint {
        fun sessionDao(): SessionDao
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderWorkerEntryPoint::class.java
        )
        val sessionDao = entryPoint.sessionDao()

        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val startEpoch = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        // Check if any completed session already exists today
        val todaySessions = sessionDao.getSessionsBetween(startEpoch, endEpoch).first()
        val hasCompletedToday = todaySessions.any { it.status == "COMPLETED" }

        if (!hasCompletedToday) {
            sendReminderNotification()
        }

        return Result.success()
    }

    private fun sendReminderNotification() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationHelper.createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ALERTS_ID)
            .setContentTitle("Light your first candle.")
            .setContentText("Dedicate calm time to your most important work today.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1003, notification)
    }
}

object ReminderScheduler {
    private const val WORK_NAME = "ember_daily_reminder_work"

    fun scheduleDailyReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val now = LocalDateTime.now()
        var target = now.with(LocalTime.of(hour, minute, 0))
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        val initialDelayMinutes = ChronoUnit.MINUTES.between(now, target)

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
