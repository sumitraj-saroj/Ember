package app.ember.focus.core.timer

import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.domain.model.Task
import app.ember.focus.domain.model.UserSettings
import app.ember.focus.domain.repository.CategoryRepository
import app.ember.focus.domain.repository.SessionRepository
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class FakeSessionRepository : SessionRepository {
    val sessions = mutableListOf<Session>()

    override fun getAllSessions(): Flow<List<Session>> = flowOf(sessions)
    override fun getSessionsForDay(epochDayStart: Long, epochDayEnd: Long): Flow<List<Session>> =
        flowOf(sessions.filter { it.startedAt in epochDayStart until epochDayEnd })
    override fun getCompletedSessionCountForTask(taskId: String): Flow<Int> =
        flowOf(sessions.count { it.taskId == taskId && it.status == SessionStatus.COMPLETED })
    override suspend fun getSessionById(id: String): Session? = sessions.find { it.id == id }
    override suspend fun saveSession(session: Session) { sessions.add(session) }
    override suspend fun updateSession(session: Session) {
        val idx = sessions.indexOfFirst { it.id == session.id }
        if (idx >= 0) sessions[idx] = session
    }
    override suspend fun deleteSession(id: String) { sessions.removeIf { it.id == id } }
}

class FakeTaskRepository : TaskRepository {
    val tasks = mutableListOf<Task>()

    override fun getActiveTasks(): Flow<List<Task>> = flowOf(tasks)
    override suspend fun getTaskById(id: String): Task? = tasks.find { it.id == id }
    override suspend fun getOrCreateTask(name: String, categoryId: String, plannedSessionCount: Int?): Task {
        val existing = tasks.find { it.name.equals(name, ignoreCase = true) }
        if (existing != null) return existing
        val newTask = Task(UUID.randomUUID().toString(), name, categoryId, plannedSessionCount)
        tasks.add(newTask)
        return newTask
    }
    override suspend fun updateTask(task: Task) {
        val idx = tasks.indexOfFirst { it.id == task.id }
        if (idx >= 0) tasks[idx] = task
    }
    override suspend fun deleteTask(id: String) { tasks.removeIf { it.id == id } }
}

class FakeCategoryRepository : CategoryRepository {
    val categories = mutableListOf(
        Category("cat_work", "Work", "#E8A15A"),
        Category("cat_study", "Study", "#E07A5F"),
        Category("cat_coding", "Coding", "#F4A261"),
        Category("cat_writing", "Writing", "#DDA15E")
    )
    override fun getAllCategories(): Flow<List<Category>> = flowOf(categories)
    override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
    override suspend fun saveCategory(category: Category) { categories.add(category) }
    override suspend fun updateCategory(category: Category) {
        val idx = categories.indexOfFirst { it.id == category.id }
        if (idx >= 0) categories[idx] = category
    }
    override suspend fun deleteCategory(id: String) { categories.removeIf { it.id == id } }
    override suspend fun ensureDefaultCategories() {}
}

class FakeSettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(UserSettings())
    override val settingsFlow: Flow<UserSettings> = _settings.asStateFlow()
    override suspend fun updateDailyGoalMinutes(minutes: Int) { _settings.value = _settings.value.copy(dailyGoalMinutes = minutes) }
    override suspend fun updateDefaultDurationMinutes(minutes: Int) { _settings.value = _settings.value.copy(defaultDurationMinutes = minutes) }
    override suspend fun updateStrictMode(enabled: Boolean) { _settings.value = _settings.value.copy(strictModeEnabled = enabled) }
    override suspend fun updateAmbientSound(enabled: Boolean, volume: Float) { _settings.value = _settings.value.copy(ambientSoundEnabled = enabled, ambientSoundVolume = volume) }
    override suspend fun updateKeepScreenOn(enabled: Boolean) { _settings.value = _settings.value.copy(keepScreenOn = enabled) }
    override suspend fun updateBreakLengths(shortBreak: Int, longBreak: Int) { _settings.value = _settings.value.copy(shortBreakMinutes = shortBreak, longBreakMinutes = longBreak) }
    override suspend fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int) { _settings.value = _settings.value.copy(dailyReminderEnabled = enabled, dailyReminderHour = hour, dailyReminderMinute = minute) }
    override suspend fun setOnboardingCompleted(completed: Boolean) { _settings.value = _settings.value.copy(onboardingCompleted = completed) }
}
