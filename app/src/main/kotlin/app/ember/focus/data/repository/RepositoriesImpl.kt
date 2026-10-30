package app.ember.focus.data.repository

import app.ember.focus.data.local.dao.CategoryDao
import app.ember.focus.data.local.dao.SessionDao
import app.ember.focus.data.local.dao.TaskDao
import app.ember.focus.data.local.entity.CategoryEntity
import app.ember.focus.data.local.entity.SessionEntity
import app.ember.focus.data.local.entity.TaskEntity
import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.domain.model.Task
import app.ember.focus.domain.repository.CategoryRepository
import app.ember.focus.domain.repository.SessionRepository
import app.ember.focus.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) : SessionRepository {

    override fun getAllSessions(): Flow<List<Session>> {
        return combine(
            sessionDao.getAllSessions(),
            taskDao.getActiveTasks(),
            categoryDao.getAllCategories()
        ) { sessions, tasks, categories ->
            val taskMap = tasks.associateBy { it.id }
            val catMap = categories.associateBy { it.id }
            sessions.map { it.toDomain(taskMap[it.taskId]?.name, catMap[it.categoryId]?.name, catMap[it.categoryId]?.colorHex) }
        }
    }

    override fun getSessionsForDay(epochDayStart: Long, epochDayEnd: Long): Flow<List<Session>> {
        return combine(
            sessionDao.getSessionsBetween(epochDayStart, epochDayEnd),
            taskDao.getActiveTasks(),
            categoryDao.getAllCategories()
        ) { sessions, tasks, categories ->
            val taskMap = tasks.associateBy { it.id }
            val catMap = categories.associateBy { it.id }
            sessions.map { it.toDomain(taskMap[it.taskId]?.name, catMap[it.categoryId]?.name, catMap[it.categoryId]?.colorHex) }
        }
    }

    override fun getCompletedSessionCountForTask(taskId: String): Flow<Int> {
        return sessionDao.getCompletedSessionsForTask(taskId).map { it.size }
    }

    override suspend fun getSessionById(id: String): Session? {
        val entity = sessionDao.getSessionById(id) ?: return null
        val task = taskDao.getTaskById(entity.taskId)
        val category = categoryDao.getCategoryById(entity.categoryId)
        return entity.toDomain(task?.name, category?.name, category?.colorHex)
    }

    override suspend fun saveSession(session: Session) {
        sessionDao.insertSession(session.toEntity())
    }

    override suspend fun updateSession(session: Session) {
        sessionDao.updateSession(session.toEntity())
    }

    override suspend fun deleteSession(id: String) {
        sessionDao.softDeleteSession(id)
    }

    private fun SessionEntity.toDomain(taskName: String?, categoryName: String?, categoryColorHex: String?): Session {
        return Session(
            id = id,
            taskId = taskId,
            taskName = taskName ?: "Deep work",
            categoryId = categoryId,
            categoryName = categoryName ?: "General",
            categoryColorHex = categoryColorHex ?: "#E8A15A",
            goal = goal,
            plannedDurationSec = plannedDurationSec,
            focusedSec = focusedSec,
            overtimeSec = overtimeSec,
            startedAt = startedAt,
            endedAt = endedAt,
            timeZoneId = timeZoneId,
            status = if (status == "COMPLETED") SessionStatus.COMPLETED else SessionStatus.BROKEN,
            goalAchieved = goalAchieved,
            rating = rating,
            note = note
        )
    }

    private fun Session.toEntity(): SessionEntity {
        return SessionEntity(
            id = id,
            taskId = taskId,
            categoryId = categoryId,
            goal = goal,
            plannedDurationSec = plannedDurationSec,
            focusedSec = focusedSec,
            overtimeSec = overtimeSec,
            startedAt = startedAt,
            endedAt = endedAt,
            timeZoneId = timeZoneId,
            status = status.name,
            goalAchieved = goalAchieved,
            rating = rating,
            note = note,
            updatedAt = System.currentTimeMillis()
        )
    }
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getActiveTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks().map { list ->
            list.map { Task(it.id, it.name, it.categoryId, it.plannedSessionCount, it.archived) }
        }
    }

    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)?.let {
            Task(it.id, it.name, it.categoryId, it.plannedSessionCount, it.archived)
        }
    }

    override suspend fun getOrCreateTask(name: String, categoryId: String, plannedSessionCount: Int?): Task {
        val trimmed = name.trim()
        val existing = taskDao.getTaskByName(trimmed)
        if (existing != null) {
            val updated = existing.copy(
                categoryId = categoryId,
                plannedSessionCount = plannedSessionCount ?: existing.plannedSessionCount,
                updatedAt = System.currentTimeMillis()
            )
            taskDao.updateTask(updated)
            return Task(updated.id, updated.name, updated.categoryId, updated.plannedSessionCount, updated.archived)
        }
        val newTask = TaskEntity(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            categoryId = categoryId,
            plannedSessionCount = plannedSessionCount
        )
        taskDao.insertTask(newTask)
        return Task(newTask.id, newTask.name, newTask.categoryId, newTask.plannedSessionCount, newTask.archived)
    }

    override suspend fun updateTask(task: Task) {
        val existing = taskDao.getTaskById(task.id) ?: return
        taskDao.updateTask(
            existing.copy(
                name = task.name.trim(),
                categoryId = task.categoryId,
                plannedSessionCount = task.plannedSessionCount,
                archived = task.archived,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteTask(id: String) {
        taskDao.softDeleteTask(id)
    }
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { Category(it.id, it.name, it.colorHex, it.sortOrder, it.archived) }
        }
    }

    override suspend fun getCategoryById(id: String): Category? {
        return categoryDao.getCategoryById(id)?.let {
            Category(it.id, it.name, it.colorHex, it.sortOrder, it.archived)
        }
    }

    override suspend fun saveCategory(category: Category) {
        categoryDao.insertCategory(
            CategoryEntity(
                id = category.id,
                name = category.name.trim(),
                colorHex = category.colorHex,
                sortOrder = category.sortOrder,
                archived = category.archived
            )
        )
    }

    override suspend fun updateCategory(category: Category) {
        val existing = categoryDao.getCategoryById(category.id) ?: return
        categoryDao.updateCategory(
            existing.copy(
                name = category.name.trim(),
                colorHex = category.colorHex,
                sortOrder = category.sortOrder,
                archived = category.archived,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteCategory(id: String) {
        categoryDao.softDeleteCategory(id)
    }

    override suspend fun ensureDefaultCategories() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaults = listOf(
                CategoryEntity("cat_work", "Work", "#E8A15A", 0),
                CategoryEntity("cat_study", "Study", "#E07A6A", 1),
                CategoryEntity("cat_coding", "Coding", "#78A9C7", 2),
                CategoryEntity("cat_writing", "Writing", "#B38FD6", 3),
                CategoryEntity("cat_reading", "Reading", "#8DBE8A", 4)
            )
            categoryDao.insertCategories(defaults)
        } else {
            // One-time color migration if still set to old seed values (G4)
            val all = categoryDao.getAllCategoriesList()
            all.forEach { cat ->
                val newColor = when {
                    cat.id == "cat_study" && cat.colorHex.equals("#E07A5F", ignoreCase = true) -> "#E07A6A"
                    cat.id == "cat_coding" && cat.colorHex.equals("#F4A261", ignoreCase = true) -> "#78A9C7"
                    cat.id == "cat_writing" && cat.colorHex.equals("#DDA15E", ignoreCase = true) -> "#B38FD6"
                    cat.id == "cat_reading" && cat.colorHex.equals("#8AB17D", ignoreCase = true) -> "#8DBE8A"
                    else -> null
                }
                if (newColor != null) {
                    categoryDao.updateCategory(cat.copy(colorHex = newColor, updatedAt = System.currentTimeMillis()))
                }
            }
        }
    }
}
