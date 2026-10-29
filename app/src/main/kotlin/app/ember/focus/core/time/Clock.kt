package app.ember.focus.core.time

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

interface Clock {
    fun nowEpochMillis(): Long
    fun elapsedRealtimeMillis(): Long
}

@Singleton
class SystemClockImpl @Inject constructor() : Clock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}
