package app.ember.focus.domain.repository

import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.Task
import app.ember.focus.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    fun getSessionsForDay(epochDayStart: Long, epochDayEnd: Long): Flow<List<Session>>
    fun getCompletedSessionCountForTask(taskId: String): Flow<Int>
    suspend fun getSessionById(id: String): Session?
    suspend fun saveSession(session: Session)
    suspend fun updateSession(session: Session)
    suspend fun deleteSession(id: String)
}

interface TaskRepository {
    fun getActiveTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: String): Task?
    suspend fun getOrCreateTask(name: String, categoryId: String, plannedSessionCount: Int? = null): Task
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: String)
}

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun saveCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: String)
    suspend fun ensureDefaultCategories()
}

interface SettingsRepository {
    val settingsFlow: Flow<UserSettings>
    suspend fun updateDailyGoalMinutes(minutes: Int)
    suspend fun updateDefaultDurationMinutes(minutes: Int)
    suspend fun updateDefaultCategoryId(categoryId: String)
    suspend fun updateStrictMode(enabled: Boolean)
    suspend fun updateAmbientSound(enabled: Boolean, volume: Float)
    suspend fun updateKeepScreenOn(enabled: Boolean)
    suspend fun updateBreakLengths(shortBreak: Int, longBreak: Int)
    suspend fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int)
    suspend fun updateReminderDays(days: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun updateFontPreset(preset: String)
}
