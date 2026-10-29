package app.ember.focus.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import app.ember.focus.core.timer.ActiveSessionState
import app.ember.focus.core.timer.TimerEngine
import app.ember.focus.core.timer.TimerEvent
import app.ember.focus.core.timer.TimerStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var timerEngine: TimerEngine

    @Inject
    lateinit var ambientSoundPlayer: app.ember.focus.core.audio.AmbientSoundPlayer

    @Inject
    lateinit var settingsRepository: app.ember.focus.domain.repository.SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var stateJob: Job? = null
    private var eventsJob: Job? = null

    companion object {
        const val ACTION_START = "app.ember.focus.action.START"
        const val ACTION_PAUSE = "app.ember.focus.action.PAUSE"
        const val ACTION_RESUME = "app.ember.focus.action.RESUME"
        const val ACTION_STOP = "app.ember.focus.action.STOP"

        const val ACTION_USER_LEFT = "app.ember.focus.action.USER_LEFT"
        const val ACTION_USER_RETURNED = "app.ember.focus.action.USER_RETURNED"

        fun startService(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun reportUserLeft(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_USER_LEFT
            }
            context.startService(intent)
        }

        fun reportUserReturned(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_USER_RETURNED
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        observeTimerState()
        observeTimerEvents()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startInForeground(timerEngine.state.value)
            }
            ACTION_PAUSE -> timerEngine.pauseSession()
            ACTION_RESUME -> timerEngine.resumeSession()
            ACTION_STOP -> stopForegroundAndService()
            ACTION_USER_LEFT -> handleUserLeft()
            ACTION_USER_RETURNED -> timerEngine.onUserReturnedToApp()
        }
        return START_STICKY
    }

    private fun handleUserLeft() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isInteractive = powerManager.isInteractive
        timerEngine.onUserLeftApp(isInteractive) { !powerManager.isInteractive }
    }

    private fun startInForeground(state: ActiveSessionState) {
        val notification = NotificationHelper.buildTimerNotification(this, state)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NotificationHelper.NOTIFICATION_TIMER_ID, notification, type)
        } else {
            startForeground(NotificationHelper.NOTIFICATION_TIMER_ID, notification)
        }
    }

    private fun observeTimerState() {
        stateJob?.cancel()
        stateJob = serviceScope.launch {
            kotlinx.coroutines.flow.combine(
                timerEngine.state,
                settingsRepository.settingsFlow
            ) { state, settings ->
                Pair(state, settings)
            }.collectLatest { (state, settings) ->
                when (state.status) {
                    TimerStatus.RUNNING, TimerStatus.OVERTIME -> {
                        val notification = NotificationHelper.buildTimerNotification(this@TimerService, state)
                        safeNotify(NotificationHelper.NOTIFICATION_TIMER_ID, notification)
                        if (state.status == TimerStatus.RUNNING && state.remainingSec > 0) {
                            scheduleExactAlarm(state.remainingSec)
                        }
                        if (settings.ambientSoundEnabled) {
                            ambientSoundPlayer.play(settings.ambientSoundVolume)
                        } else {
                            ambientSoundPlayer.stop()
                        }
                    }
                    TimerStatus.PAUSED -> {
                        val notification = NotificationHelper.buildTimerNotification(this@TimerService, state)
                        safeNotify(NotificationHelper.NOTIFICATION_TIMER_ID, notification)
                        ambientSoundPlayer.pause()
                    }
                    TimerStatus.COMPLETED, TimerStatus.BROKEN, TimerStatus.IDLE -> {
                        cancelExactAlarm()
                        ambientSoundPlayer.stop()
                        if (state.status == TimerStatus.IDLE) {
                            stopForegroundAndService()
                        }
                    }
                }
            }
        }
    }

    private fun observeTimerEvents() {
        eventsJob?.cancel()
        eventsJob = serviceScope.launch {
            timerEngine.events.collectLatest { event ->
                when (event) {
                    is TimerEvent.OvertimeReached -> {
                        NotificationHelper.showOvertimeAlert(this@TimerService)
                    }
                    is TimerEvent.CandleWentOut -> {
                        NotificationHelper.showBrokenAlert(this@TimerService, event.minutesCounted)
                    }
                    is TimerEvent.SessionCompleted -> {
                        // Handled in UI
                    }
                }
            }
        }
    }

    private fun scheduleExactAlarm(remainingSec: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTimeMs = System.currentTimeMillis() + (remainingSec * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
        } catch (_: SecurityException) {
            // Graceful fallback if permission is denied
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }
    }

    private fun cancelExactAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            101,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun safeNotify(id: Int, notification: android.app.Notification) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this).notify(id, notification)
            }
        } catch (_: SecurityException) {
            // Permission rejected or revoked by user
        }
    }

    private fun stopForegroundAndService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
