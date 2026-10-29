package app.ember.focus.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ember.focus.core.timer.ActiveSessionState
import app.ember.focus.core.timer.TimerEngine
import app.ember.focus.core.timer.TimerStatus
import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.domain.model.Task
import app.ember.focus.domain.model.UserSettings
import app.ember.focus.domain.repository.CategoryRepository
import app.ember.focus.domain.repository.SessionRepository
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.domain.repository.TaskRepository
import app.ember.focus.domain.util.calculateStreaks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

data class TodayUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val sessions: List<Session> = emptyList(),
    val totalFocusedMinutes: Int = 0,
    val dailyGoalMinutes: Int = 240,
    val completedCount: Int = 0,
    val brokenCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val highlightedSessionId: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val timerEngine: TimerEngine
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    val timerState: StateFlow<ActiveSessionState> = timerEngine.state
    val recentTasks: StateFlow<List<Task>> = taskRepository.getActiveTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _highlightedSessionId = MutableStateFlow<String?>(null)
    val highlightedSessionId: StateFlow<String?> = _highlightedSessionId.asStateFlow()

    private val _showStartSheet = MutableStateFlow(false)
    val showStartSheet: StateFlow<Boolean> = _showStartSheet.asStateFlow()

    private val _selectedDetailSession = MutableStateFlow<Session?>(null)
    val selectedDetailSession: StateFlow<Session?> = _selectedDetailSession.asStateFlow()

    val uiState: StateFlow<TodayUiState> = combine(
        _selectedDate,
        _selectedDate.flatMapLatest { date ->
            val startEpoch = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endEpoch = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            sessionRepository.getSessionsForDay(startEpoch, endEpoch)
        },
        sessionRepository.getAllSessions(),
        settingsRepository.settingsFlow,
        _highlightedSessionId
    ) { date, daySessions, allSessions, userSettings, highlightedId ->
        val totalSec = daySessions.sumOf { it.focusedSec }
        val completed = daySessions.count { it.status == SessionStatus.COMPLETED }
        val broken = daySessions.count { it.status == SessionStatus.BROKEN }

        // Compute streak from all sessions
        val dayMinutesMap = allSessions.groupBy {
            Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate()
        }.mapValues { entry ->
            entry.value.sumOf { it.focusedSec } / 60
        }

        val (currentStreak, bestStreak) = calculateStreaks(
            dayMinutesMap = dayMinutesMap,
            today = LocalDate.now(),
            dailyGoalMinutes = userSettings.dailyGoalMinutes
        )

        TodayUiState(
            selectedDate = date,
            sessions = daySessions,
            totalFocusedMinutes = totalSec / 60,
            dailyGoalMinutes = userSettings.dailyGoalMinutes,
            completedCount = completed,
            brokenCount = broken,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            highlightedSessionId = highlightedId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _highlightedSessionId.value = null
    }

    fun nextDay() {
        if (_selectedDate.value.isBefore(LocalDate.now())) {
            _selectedDate.value = _selectedDate.value.plusDays(1)
        }
    }

    fun prevDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun highlightSession(sessionId: String) {
        _highlightedSessionId.value = sessionId
    }

    fun openStartSheet() {
        _showStartSheet.value = true
    }

    fun dismissStartSheet() {
        _showStartSheet.value = false
    }

    fun openDetailSheet(session: Session) {
        _selectedDetailSession.value = session
    }

    fun dismissDetailSheet() {
        _selectedDetailSession.value = null
    }

    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            val newCat = Category(
                id = UUID.randomUUID().toString(),
                name = name,
                colorHex = colorHex,
                sortOrder = categories.value.size
            )
            categoryRepository.saveCategory(newCat)
        }
    }

    fun startSession(
        taskName: String,
        categoryId: String,
        goal: String?,
        durationMinutes: Int,
        plannedSessionCount: Int?,
        onSessionStarted: () -> Unit
    ) {
        _showStartSheet.value = false
        timerEngine.startSession(
            taskName = taskName,
            categoryId = categoryId,
            goal = goal,
            durationMinutes = durationMinutes,
            plannedSessionCount = plannedSessionCount
        )
        onSessionStarted()
    }

    fun updateSession(session: Session) {
        viewModelScope.launch {
            sessionRepository.updateSession(session)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }
}
