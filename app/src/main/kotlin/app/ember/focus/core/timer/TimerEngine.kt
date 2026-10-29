package app.ember.focus.core.timer

import app.ember.focus.core.time.Clock
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.domain.repository.CategoryRepository
import app.ember.focus.domain.repository.SessionRepository
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class TimerEvent {
    data object OvertimeReached : TimerEvent()
    data class CandleWentOut(val minutesCounted: Int) : TimerEvent()
    data class SessionCompleted(val session: Session) : TimerEvent()
}

@Singleton
class TimerEngine @Inject constructor(
    private val clock: Clock,
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) {
    var dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default
    var enableTicker: Boolean = true
    private val scope by lazy { CoroutineScope(dispatcher) }

    private val _state = MutableStateFlow(ActiveSessionState())
    val state: StateFlow<ActiveSessionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    private var tickerJob: Job? = null
    private var graceJob: Job? = null

    // For reboot reconciliation
    var lastKnownBootTime: Long = 0L
        private set

    init {
        lastKnownBootTime = clock.nowEpochMillis() - clock.elapsedRealtimeMillis()
    }

    fun startSession(
        taskName: String,
        categoryId: String,
        goal: String?,
        durationMinutes: Int,
        plannedSessionCount: Int?
    ) {
        val nowEpoch = clock.nowEpochMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()
        val durationSec = durationMinutes * 60
        val sessionId = UUID.randomUUID().toString()

        scope.launch {
            val task = taskRepository.getOrCreateTask(taskName, categoryId, plannedSessionCount)
            val category = categoryRepository.getCategoryById(categoryId)
            val completedCount = sessionRepository.getCompletedSessionCountForTask(task.id).first()

            val newState = ActiveSessionState(
                status = TimerStatus.RUNNING,
                sessionId = sessionId,
                taskId = task.id,
                taskName = task.name,
                categoryId = categoryId,
                categoryName = category?.name ?: "General",
                categoryColorHex = category?.colorHex ?: "#E8A15A",
                goal = goal,
                plannedSessionCount = task.plannedSessionCount,
                completedSessionsCount = completedCount,
                plannedDurationSec = durationSec,
                startedAtEpochMs = nowEpoch,
                lastHeartbeatEpochMs = nowEpoch,
                lastStateChangeElapsedMs = nowElapsed,
                accumulatedFocusedSec = 0,
                accumulatedPausedSec = 0,
                remainingSec = durationSec,
                overtimeSec = 0,
                progress = 0f,
                candleSeed = sessionId
            )
            _state.value = newState
            startTicker()
        }
    }

    fun pauseSession() {
        val current = _state.value
        if (current.status != TimerStatus.RUNNING && current.status != TimerStatus.OVERTIME) return

        cancelGraceTimer()
        tickerJob?.cancel()
        val nowElapsed = clock.elapsedRealtimeMillis()
        val focusedInThisRun = ((nowElapsed - current.lastStateChangeElapsedMs) / 1000L).toInt().coerceAtLeast(0)
        val newAccumulated = current.accumulatedFocusedSec + focusedInThisRun

        _state.value = current.copy(
            status = TimerStatus.PAUSED,
            accumulatedFocusedSec = newAccumulated,
            lastStateChangeElapsedMs = nowElapsed,
            lastHeartbeatEpochMs = clock.nowEpochMillis()
        )
    }

    fun resumeSession() {
        val current = _state.value
        if (current.status != TimerStatus.PAUSED) return

        val nowElapsed = clock.elapsedRealtimeMillis()
        val pausedInThisRun = ((nowElapsed - current.lastStateChangeElapsedMs) / 1000L).toInt().coerceAtLeast(0)
        val newAccumulatedPaused = current.accumulatedPausedSec + pausedInThisRun

        val newStatus = if (current.remainingSec <= 0) TimerStatus.OVERTIME else TimerStatus.RUNNING
        _state.value = current.copy(
            status = newStatus,
            accumulatedPausedSec = newAccumulatedPaused,
            lastStateChangeElapsedMs = nowElapsed,
            lastHeartbeatEpochMs = clock.nowEpochMillis()
        )
        startTicker()
    }

    /**
     * Resets the active session.
     * If >= 1 minute (60s) had burned, logs the session as BROKEN with the minutes burned so far.
     * Otherwise discards it.
     * Returns true if session was logged as broken, false if discarded.
     */
    fun resetSession(): Boolean {
        cancelGraceTimer()
        tickerJob?.cancel()

        val current = _state.value
        val burnedSec = current.totalFocusedSec

        val loggedAsBroken = if (burnedSec >= 60 && current.status != TimerStatus.IDLE) {
            val session = Session(
                id = current.sessionId.ifBlank { UUID.randomUUID().toString() },
                taskId = current.taskId,
                taskName = current.taskName,
                categoryId = current.categoryId,
                categoryName = current.categoryName,
                categoryColorHex = current.categoryColorHex,
                goal = current.goal,
                plannedDurationSec = current.plannedDurationSec,
                focusedSec = burnedSec,
                overtimeSec = current.overtimeSec,
                startedAt = current.startedAtEpochMs,
                endedAt = clock.nowEpochMillis(),
                timeZoneId = TimeZone.getDefault().id,
                status = SessionStatus.BROKEN,
                goalAchieved = false,
                rating = null,
                note = null
            )
            scope.launch {
                sessionRepository.saveSession(session)
            }
            true
        } else {
            false
        }

        // Show fresh unlit candle with the same task
        _state.value = ActiveSessionState(
            status = TimerStatus.IDLE,
            taskId = current.taskId,
            taskName = current.taskName,
            categoryId = current.categoryId,
            categoryName = current.categoryName,
            categoryColorHex = current.categoryColorHex,
            goal = current.goal,
            plannedSessionCount = current.plannedSessionCount,
            completedSessionsCount = current.completedSessionsCount,
            plannedDurationSec = current.plannedDurationSec,
            remainingSec = current.plannedDurationSec,
            candleSeed = UUID.randomUUID().toString()
        )

        return loggedAsBroken
    }

    fun finishSession() {
        val current = _state.value
        if (current.status == TimerStatus.IDLE || current.status == TimerStatus.COMPLETED) return

        cancelGraceTimer()
        tickerJob?.cancel()

        val totalFocusedSec = current.totalFocusedSec
        val session = Session(
            id = current.sessionId.ifBlank { UUID.randomUUID().toString() },
            taskId = current.taskId,
            taskName = current.taskName,
            categoryId = current.categoryId,
            categoryName = current.categoryName,
            categoryColorHex = current.categoryColorHex,
            goal = current.goal,
            plannedDurationSec = current.plannedDurationSec,
            focusedSec = totalFocusedSec,
            overtimeSec = current.overtimeSec,
            startedAt = current.startedAtEpochMs,
            endedAt = clock.nowEpochMillis(),
            timeZoneId = TimeZone.getDefault().id,
            status = SessionStatus.COMPLETED,
            goalAchieved = if (current.goal != null) true else null,
            rating = null,
            note = null
        )

        _state.value = current.copy(
            status = TimerStatus.COMPLETED,
            lastHeartbeatEpochMs = clock.nowEpochMillis()
        )

        scope.launch {
            sessionRepository.saveSession(session)
            _events.emit(TimerEvent.SessionCompleted(session))
        }
    }

    /**
     * Strict Mode Handler:
     * Called when the Activity stops while device is interactive (screen ON).
     */
    fun onUserLeftApp(isInteractive: Boolean, isScreenOff: () -> Boolean) {
        val current = _state.value
        if (current.status != TimerStatus.RUNNING && current.status != TimerStatus.OVERTIME) {
            return
        }

        scope.launch {
            val settings = settingsRepository.settingsFlow.first()
            if (!settings.strictModeEnabled) return@launch

            // If screen is interactive (user switched apps / went home while screen was ON)
            if (isInteractive) {
                startGraceTimer(isScreenOff)
            }
        }
    }

    fun onUserReturnedToApp() {
        cancelGraceTimer()
    }

    private fun startGraceTimer(isScreenOff: () -> Boolean) {
        if (graceJob?.isActive == true) return

        _state.value = _state.value.copy(
            isGraceTimerRunning = true,
            graceRemainingSec = 10
        )

        graceJob = scope.launch {
            for (sec in 10 downTo 1) {
                _state.value = _state.value.copy(graceRemainingSec = sec)
                delay(1000L)
            }

            // Check if screen was turned off at the moment grace expires
            if (isScreenOff()) {
                cancelGraceTimer()
                return@launch
            }

            // Grace timer expired! Blow out the candle!
            snuffCandle()
        }
    }

    private fun cancelGraceTimer() {
        graceJob?.cancel()
        graceJob = null
        _state.value = _state.value.copy(isGraceTimerRunning = false, graceRemainingSec = 10)
    }

    private fun snuffCandle() {
        cancelGraceTimer()
        tickerJob?.cancel()

        val current = _state.value
        val burnedSec = current.totalFocusedSec

        val session = Session(
            id = current.sessionId.ifBlank { UUID.randomUUID().toString() },
            taskId = current.taskId,
            taskName = current.taskName,
            categoryId = current.categoryId,
            categoryName = current.categoryName,
            categoryColorHex = current.categoryColorHex,
            goal = current.goal,
            plannedDurationSec = current.plannedDurationSec,
            focusedSec = burnedSec,
            overtimeSec = current.overtimeSec,
            startedAt = current.startedAtEpochMs,
            endedAt = clock.nowEpochMillis(),
            timeZoneId = TimeZone.getDefault().id,
            status = SessionStatus.BROKEN,
            goalAchieved = false,
            rating = null,
            note = null
        )

        _state.value = current.copy(
            status = TimerStatus.BROKEN,
            accumulatedFocusedSec = burnedSec,
            lastHeartbeatEpochMs = clock.nowEpochMillis()
        )

        scope.launch {
            sessionRepository.saveSession(session)
            _events.emit(TimerEvent.CandleWentOut(burnedSec / 60))
        }
    }

    fun reconcileOnStartup(rebootDetected: Boolean) {
        val current = _state.value
        if (rebootDetected && (current.status == TimerStatus.RUNNING || current.status == TimerStatus.PAUSED || current.status == TimerStatus.OVERTIME)) {
            // Device rebooted mid-session: mark as BROKEN
            snuffCandle()
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        if (!enableTicker) return
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                updateTick()
            }
        }
    }

    fun updateTick() {
        val current = _state.value
        val nowElapsed = clock.elapsedRealtimeMillis()
        val nowEpoch = clock.nowEpochMillis()

        if (current.status == TimerStatus.RUNNING) {
            val totalRunningElapsedSec = ((nowElapsed - current.lastStateChangeElapsedMs) / 1000L).toInt()
            val totalFocusedSec = current.accumulatedFocusedSec + totalRunningElapsedSec
            val remaining = (current.plannedDurationSec - totalFocusedSec).coerceAtLeast(0)
            val progress = (totalFocusedSec.toFloat() / current.plannedDurationSec.toFloat()).coerceIn(0f, 1f)

            if (remaining <= 0) {
                // Reached 00:00, transition to OVERTIME
                _state.value = current.copy(
                    status = TimerStatus.OVERTIME,
                    remainingSec = 0,
                    overtimeSec = 0,
                    progress = 1.0f,
                    lastStateChangeElapsedMs = nowElapsed,
                    lastHeartbeatEpochMs = nowEpoch,
                    accumulatedFocusedSec = current.plannedDurationSec
                )
                scope.launch {
                    _events.emit(TimerEvent.OvertimeReached)
                }
            } else {
                _state.value = current.copy(
                    remainingSec = remaining,
                    progress = progress,
                    lastHeartbeatEpochMs = nowEpoch
                )
            }
        } else if (current.status == TimerStatus.OVERTIME) {
            val overtimeElapsedSec = ((nowElapsed - current.lastStateChangeElapsedMs) / 1000L).toInt()
            _state.value = current.copy(
                overtimeSec = overtimeElapsedSec,
                progress = 1.0f,
                lastHeartbeatEpochMs = nowEpoch
            )
        }
    }
}
