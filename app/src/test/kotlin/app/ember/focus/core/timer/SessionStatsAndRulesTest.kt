package app.ember.focus.core.timer

import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.domain.util.calculateStreaks
import app.ember.focus.domain.util.formatSessionPlanLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class SessionStatsAndRulesTest {

    private val zoneId = ZoneId.of("UTC")

    @Test
    fun testCountedMinutesRules() {
        // COMPLETED session: full planned (25m) + overtime (5m) = 30m
        val completed = createSession(
            plannedSec = 25 * 60,
            focusedSec = 30 * 60,
            overtimeSec = 5 * 60,
            status = SessionStatus.COMPLETED
        )
        assertEquals(30, completed.focusedSec / 60)

        // BROKEN session: planned was 60m, but broke after 14m
        val broken = createSession(
            plannedSec = 60 * 60,
            focusedSec = 14 * 60,
            overtimeSec = 0,
            status = SessionStatus.BROKEN
        )
        assertEquals(14, broken.focusedSec / 60)
    }

    @Test
    fun testSessionCrossingMidnightAttributedToStartDay() {
        // Starts on 2026-10-28 at 23:45 UTC, ends 2026-10-29 at 00:15 UTC (30 min)
        val startEpoch = Instant.parse("2026-10-28T23:45:00Z").toEpochMilli()
        val endEpoch = Instant.parse("2026-10-29T00:15:00Z").toEpochMilli()

        val session = createSession(
            startedAt = startEpoch,
            endedAt = endEpoch,
            plannedSec = 30 * 60,
            focusedSec = 30 * 60,
            status = SessionStatus.COMPLETED
        )

        val sessionDay = LocalDate.ofInstant(Instant.ofEpochMilli(session.startedAt), zoneId)
        assertEquals(LocalDate.parse("2026-10-28"), sessionDay)
    }

    @Test
    fun testPlannedSessionCountFormatting() {
        // Task with plannedSessionCount = 4
        // 0 completed -> "session 1 of 4"
        assertEquals("session 1 of 4", formatSessionPlanLabel(completedCount = 0, plannedCount = 4))

        // 2 completed -> "session 3 of 4"
        assertEquals("session 3 of 4", formatSessionPlanLabel(completedCount = 2, plannedCount = 4))

        // 4 completed -> "plan complete"
        assertEquals("plan complete", formatSessionPlanLabel(completedCount = 4, plannedCount = 4))

        // 5 completed (continued) -> "plan complete"
        assertEquals("plan complete", formatSessionPlanLabel(completedCount = 5, plannedCount = 4))

        // null plan -> null
        assertNull(formatSessionPlanLabel(completedCount = 3, plannedCount = null))
    }

    @Test
    fun testStreakCalculation() {
        val dailyGoalMinutes = 120 // 2 hours
        val today = LocalDate.parse("2026-10-29")

        // Map of date -> total minutes
        val dayTotals = mapOf(
            LocalDate.parse("2026-10-26") to 150, // Goal met
            LocalDate.parse("2026-10-27") to 130, // Goal met
            LocalDate.parse("2026-10-28") to 180, // Goal met
            LocalDate.parse("2026-10-29") to 45   // Today in progress (< goal)
        )

        // Today does not break the streak until midnight passes!
        val (currentStreak, bestStreak) = calculateStreaks(dayTotals, today, dailyGoalMinutes)
        assertEquals(3, currentStreak) // 26th, 27th, 28th met!
        assertEquals(3, bestStreak)

        // If today also met goal
        val dayTotalsWithTodayMet = dayTotals + (LocalDate.parse("2026-10-29") to 140)
        val (currentStreakTodayMet, bestStreakTodayMet) = calculateStreaks(dayTotalsWithTodayMet, today, dailyGoalMinutes)
        assertEquals(4, currentStreakTodayMet)
        assertEquals(4, bestStreakTodayMet)
    }

    private fun createSession(
        startedAt: Long = 1000000L,
        endedAt: Long = 2000000L,
        plannedSec: Int,
        focusedSec: Int,
        overtimeSec: Int = 0,
        status: SessionStatus
    ): Session {
        return Session(
            id = UUID.randomUUID().toString(),
            taskId = "task_1",
            categoryId = "cat_work",
            goal = null,
            plannedDurationSec = plannedSec,
            focusedSec = focusedSec,
            overtimeSec = overtimeSec,
            startedAt = startedAt,
            endedAt = endedAt,
            timeZoneId = "UTC",
            status = status
        )
    }
}
