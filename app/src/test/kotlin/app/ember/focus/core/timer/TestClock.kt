package app.ember.focus.core.timer

import app.ember.focus.core.time.Clock

class TestClock(
    var currentEpochMillis: Long = 1700000000000L,
    var currentElapsedMillis: Long = 100000L
) : Clock {
    override fun nowEpochMillis(): Long = currentEpochMillis
    override fun elapsedRealtimeMillis(): Long = currentElapsedMillis

    fun advanceTimeSeconds(seconds: Long) {
        currentEpochMillis += (seconds * 1000L)
        currentElapsedMillis += (seconds * 1000L)
    }
}
