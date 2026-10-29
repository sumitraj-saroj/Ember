package app.ember.focus.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Locale
import app.ember.focus.MainActivity
import app.ember.focus.R
import app.ember.focus.core.timer.ActiveSessionState
import app.ember.focus.core.timer.TimerStatus

object NotificationHelper {
    const val CHANNEL_TIMER_ID = "ember_timer_channel"
    const val CHANNEL_ALERTS_ID = "ember_alerts_channel"
    const val NOTIFICATION_TIMER_ID = 1001
    const val NOTIFICATION_ALERT_ID = 1002

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val timerChannel = NotificationChannel(
            CHANNEL_TIMER_ID,
            "Active Focus Session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows remaining time on burning candle"
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
        }

        val alertsChannel = NotificationChannel(
            CHANNEL_ALERTS_ID,
            "Ember Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifies when candle completes or goes out"
        }

        manager.createNotificationChannel(timerChannel)
        manager.createNotificationChannel(alertsChannel)
    }

    fun buildTimerNotification(context: Context, state: ActiveSessionState): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = state.taskName.ifBlank { "Deep work" }
        val contentText = when (state.status) {
            TimerStatus.RUNNING -> {
                val m = state.remainingSec / 60
                val s = state.remainingSec % 60
                String.format(Locale.US, "%02d:%02d remaining · Candle burning", m, s)
            }
            TimerStatus.PAUSED -> {
                val m = state.remainingSec / 60
                val s = state.remainingSec % 60
                String.format(Locale.US, "Paused · %02d:%02d remaining", m, s)
            }
            TimerStatus.OVERTIME -> {
                val m = state.overtimeSec / 60
                val s = state.overtimeSec % 60
                String.format(Locale.US, "In flow · +%02d:%02d", m, s)
            }
            TimerStatus.COMPLETED -> "Session completed · take five"
            TimerStatus.BROKEN -> "Candle went out · ${state.totalFocusedMinutes} min counted"
            TimerStatus.IDLE -> "Ember · Deep focus"
        }

        return NotificationCompat.Builder(context, CHANNEL_TIMER_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(state.status == TimerStatus.RUNNING || state.status == TimerStatus.PAUSED || state.status == TimerStatus.OVERTIME)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun showOvertimeAlert(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setContentTitle("Planned time is up")
            .setContentText("Still in flow? Overtime is counting.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(NOTIFICATION_ALERT_ID, notification)
        } catch (_: SecurityException) {}
    }

    fun showBrokenAlert(context: Context, minutesCounted: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setContentTitle("Your candle went out")
            .setContentText("$minutesCounted min counted toward your daily focus.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(NOTIFICATION_ALERT_ID, notification)
        } catch (_: SecurityException) {}
    }
}
