package app.ember.focus.core.timer

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    OVERTIME,
    COMPLETED,
    BROKEN
}

data class ActiveSessionState(
    val status: TimerStatus = TimerStatus.IDLE,
    val sessionId: String = "",
    val taskId: String = "",
    val taskName: String = "Deep work",
    val categoryId: String = "",
    val categoryName: String = "General",
    val categoryColorHex: String = "#E8A15A",
    val goal: String? = null,
    val plannedSessionCount: Int? = null,
    val completedSessionsCount: Int = 0,
    val plannedDurationSec: Int = 25 * 60,
    val startedAtEpochMs: Long = 0L,
    val lastHeartbeatEpochMs: Long = 0L,
    val lastStateChangeElapsedMs: Long = 0L,
    val accumulatedFocusedSec: Int = 0,
    val accumulatedPausedSec: Int = 0,
    val remainingSec: Int = 25 * 60,
    val overtimeSec: Int = 0,
    val progress: Float = 0f,
    val isGraceTimerRunning: Boolean = false,
    val graceRemainingSec: Int = 10,
    val candleSeed: String = "ember_session"
) {
    val totalFocusedSec: Int
        get() = when (status) {
            TimerStatus.OVERTIME, TimerStatus.COMPLETED -> plannedDurationSec + overtimeSec
            TimerStatus.RUNNING -> (plannedDurationSec - remainingSec).coerceAtLeast(0)
            TimerStatus.PAUSED, TimerStatus.BROKEN -> accumulatedFocusedSec
            TimerStatus.IDLE -> 0
        }

    val totalFocusedMinutes: Int
        get() = totalFocusedSec / 60
}
