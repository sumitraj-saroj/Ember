package app.ember.focus.domain.util

import java.time.LocalDate

fun formatSessionPlanLabel(completedCount: Int, plannedCount: Int?): String? {
    if (plannedCount == null) return null
    val current = completedCount + 1
    return if (current > plannedCount) {
        "plan complete"
    } else {
        "session $current of $plannedCount"
    }
}

fun calculateStreaks(
    dayMinutesMap: Map<LocalDate, Int>,
    today: LocalDate,
    dailyGoalMinutes: Int
): Pair<Int, Int> {
    var currentStreak = 0
    var bestStreak = 0
    var runningStreak = 0

    val todayMet = (dayMinutesMap[today] ?: 0) >= dailyGoalMinutes
    var checkDate = if (todayMet) today else today.minusDays(1)

    while (true) {
        val minutes = dayMinutesMap[checkDate] ?: 0
        if (minutes >= dailyGoalMinutes) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        } else {
            break
        }
    }

    val sortedDates = dayMinutesMap.keys.sorted()
    var prevDate: LocalDate? = null

    for (date in sortedDates) {
        val met = (dayMinutesMap[date] ?: 0) >= dailyGoalMinutes
        if (met) {
            if (prevDate != null && date == prevDate.plusDays(1)) {
                runningStreak++
            } else {
                runningStreak = 1
            }
            if (runningStreak > bestStreak) {
                bestStreak = runningStreak
            }
        } else {
            runningStreak = 0
        }
        prevDate = date
    }

    bestStreak = maxOf(bestStreak, currentStreak)
    return Pair(currentStreak, bestStreak)
}
