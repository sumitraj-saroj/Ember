package app.ember.focus.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.domain.model.UserSettings
import app.ember.focus.domain.repository.CategoryRepository
import app.ember.focus.domain.repository.SessionRepository
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.domain.util.calculateStreaks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class StatsTab {
    WEEK,
    MONTH
}

data class CategoryShare(
    val categoryId: String,
    val categoryName: String,
    val colorHex: String,
    val totalMinutes: Int,
    val percentage: Float
)

data class StatsUiState(
    val selectedTab: StatsTab = StatsTab.WEEK,
    val dailyGoalMinutes: Int = 240,
    val weekDayStats: List<DayStat> = emptyList(),
    val monthDayStats: List<DayStat> = emptyList(),
    val rangeTotalMinutes: Int = 0,
    val rangeDailyAverageMinutes: Int = 0,
    val bestDayDescription: String? = null,
    val categoryShares: List<CategoryShare> = emptyList(),
    val averageRating: Float? = null,
    val completionRatePercentage: Int? = null,
    val longestSessionMinutes: Int? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val inspectedDay: DayStat? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    private val _selectedTab = MutableStateFlow(StatsTab.WEEK)
    val selectedTab: StateFlow<StatsTab> = _selectedTab.asStateFlow()

    private val _inspectedDay = MutableStateFlow<DayStat?>(null)
    val inspectedDay: StateFlow<DayStat?> = _inspectedDay.asStateFlow()

    val uiState: StateFlow<StatsUiState> = combine(
        _selectedTab,
        sessionRepository.getAllSessions(),
        categoryRepository.getAllCategories(),
        settingsRepository.settingsFlow,
        _inspectedDay
    ) { tab, allSessions, categories, settings, inspected ->
        val today = LocalDate.now()
        val dailyGoal = settings.dailyGoalMinutes

        // Group sessions by LocalDate
        val dayMap = allSessions.groupBy {
            Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate()
        }.mapValues { entry ->
            entry.value.sumOf { it.focusedSec } / 60
        }

        // Streak computation
        val (currentStreak, bestStreak) = calculateStreaks(dayMap, today, dailyGoal)

        // 1. Week Stats (Mon to Sun of current week)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDayStats = (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val label = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Mon"
                DayOfWeek.TUESDAY -> "Tue"
                DayOfWeek.WEDNESDAY -> "Wed"
                DayOfWeek.THURSDAY -> "Thu"
                DayOfWeek.FRIDAY -> "Fri"
                DayOfWeek.SATURDAY -> "Sat"
                DayOfWeek.SUNDAY -> "Sun"
            }
            DayStat(date, label, dayMap[date] ?: 0, isToday = date == today)
        }

        // 2. Month Stats
        val currentYearMonth = YearMonth.from(today)
        val daysInMonthCount = currentYearMonth.lengthOfMonth()
        val monthDayStats = (1..daysInMonthCount).map { dayNum ->
            val date = currentYearMonth.atDay(dayNum)
            DayStat(date, dayNum.toString(), dayMap[date] ?: 0, isToday = date == today)
        }

        // Select active range based on tab
        val relevantDates = if (tab == StatsTab.WEEK) {
            weekDayStats.map { it.date }
        } else {
            monthDayStats.map { it.date }
        }

        val rangeSessions = allSessions.filter {
            val d = Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate()
            d in relevantDates
        }

        val rangeTotalMinutes = relevantDates.sumOf { dayMap[it] ?: 0 }
        val rangeDailyAverageMinutes = if (relevantDates.isNotEmpty()) {
            rangeTotalMinutes / relevantDates.size
        } else 0

        // Best Day: undefined/null if zero sessions
        val bestStat = (if (tab == StatsTab.WEEK) weekDayStats else monthDayStats).maxByOrNull { it.minutes }
        val bestDayDesc = if (bestStat != null && bestStat.minutes > 0) {
            val h = bestStat.minutes / 60
            val m = bestStat.minutes % 60
            "${bestStat.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} (${if (h > 0) "${h}h ${m}m" else "${m}m"})"
        } else {
            null
        }

        // Category Shares
        val catMap = categories.associateBy { it.id }
        val categoryTotals = rangeSessions.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.focusedSec } / 60 }

        val categoryShares = categoryTotals.mapNotNull { (catId, minutes) ->
            val cat = catMap[catId]
            if (cat != null && minutes > 0) {
                val pct = if (rangeTotalMinutes > 0) (minutes.toFloat() / rangeTotalMinutes.toFloat()) else 0f
                CategoryShare(catId, cat.name, cat.colorHex, minutes, pct)
            } else null
        }.sortedByDescending { it.totalMinutes }

        // Average Rating: undefined/null if zero rated sessions
        val ratedSessions = rangeSessions.mapNotNull { it.rating }
        val avgRating = if (ratedSessions.isNotEmpty()) {
            ratedSessions.average().toFloat()
        } else null

        // Completion Rate: completed / (completed + broken), undefined when there are none (ST3)
        val completedCount = rangeSessions.count { it.status == SessionStatus.COMPLETED }
        val brokenCount = rangeSessions.count { it.status == SessionStatus.BROKEN }
        val finishedCount = completedCount + brokenCount
        val completionRate = if (finishedCount > 0) {
            ((completedCount.toFloat() / finishedCount.toFloat()) * 100).toInt()
        } else null

        // Longest session: undefined/null if zero sessions
        val longest = if (rangeSessions.isNotEmpty() && rangeSessions.any { it.focusedSec > 0 }) {
            rangeSessions.maxOf { it.focusedSec / 60 }
        } else null

        StatsUiState(
            selectedTab = tab,
            dailyGoalMinutes = dailyGoal,
            weekDayStats = weekDayStats,
            monthDayStats = monthDayStats,
            rangeTotalMinutes = rangeTotalMinutes,
            rangeDailyAverageMinutes = rangeDailyAverageMinutes,
            bestDayDescription = bestDayDesc,
            categoryShares = categoryShares,
            averageRating = avgRating,
            completionRatePercentage = completionRate,
            longestSessionMinutes = longest,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            inspectedDay = inspected
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun selectTab(tab: StatsTab) {
        _selectedTab.value = tab
        _inspectedDay.value = null
    }

    fun inspectDay(stat: DayStat?) {
        _inspectedDay.value = stat
    }
}
