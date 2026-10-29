package app.ember.focus.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.ember.focus.core.timer.TimerEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_TIMER_EXPIRED = "app.ember.focus.action.TIMER_EXPIRED"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_TIMER_EXPIRED && context != null) {
            NotificationHelper.showOvertimeAlert(context)
        }
    }
}

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var timerEngine: TimerEngine

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            timerEngine.reconcileOnStartup(rebootDetected = true)
        }
    }
}
