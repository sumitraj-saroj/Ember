package app.ember.focus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sessions",
    indices = [
        Index("startedAt"),
        Index("taskId"),
        Index("categoryId"),
        Index("isDeleted")
    ]
)
data class SessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val categoryId: String,
    val goal: String? = null,
    val plannedDurationSec: Int,
    val focusedSec: Int,
    val overtimeSec: Int = 0,
    val startedAt: Long,
    val endedAt: Long,
    val timeZoneId: String,
    val status: String, // "COMPLETED" or "BROKEN"
    val goalAchieved: Boolean? = null,
    val rating: Int? = null, // 1 to 5
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "tasks",
    indices = [
        Index("name", unique = true),
        Index("categoryId"),
        Index("isDeleted")
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val categoryId: String,
    val plannedSessionCount: Int? = null,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "categories",
    indices = [
        Index("sortOrder"),
        Index("isDeleted")
    ]
)
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String,
    val sortOrder: Int = 0,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
