package app.ember.focus.domain.model

enum class SessionStatus {
    COMPLETED,
    BROKEN
}

data class Session(
    val id: String,
    val taskId: String,
    val taskName: String = "",
    val categoryId: String,
    val categoryName: String = "",
    val categoryColorHex: String = "#E8A15A",
    val goal: String? = null,
    val plannedDurationSec: Int,
    val focusedSec: Int,
    val overtimeSec: Int = 0,
    val startedAt: Long,
    val endedAt: Long,
    val timeZoneId: String,
    val status: SessionStatus,
    val goalAchieved: Boolean? = null,
    val rating: Int? = null,
    val note: String? = null
)

data class Task(
    val id: String,
    val name: String,
    val categoryId: String,
    val plannedSessionCount: Int? = null,
    val archived: Boolean = false
)

data class Category(
    val id: String,
    val name: String,
    val colorHex: String,
    val sortOrder: Int = 0,
    val archived: Boolean = false
)

data class UserSettings(
    val dailyGoalMinutes: Int = 240, // 4 hours
    val defaultDurationMinutes: Int = 25,
    val strictModeEnabled: Boolean = true,
    val ambientSoundEnabled: Boolean = true,
    val ambientSoundVolume: Float = 0.5f,
    val keepScreenOn: Boolean = true,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 9,
    val dailyReminderMinute: Int = 0,
    val onboardingCompleted: Boolean = false
)
