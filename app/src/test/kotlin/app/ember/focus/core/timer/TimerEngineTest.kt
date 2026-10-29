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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class TimerEngineTest {

    private lateinit var testClock: TestClock
    private lateinit var fakeSessionRepo: FakeSessionRepository
    private lateinit var fakeTaskRepo: FakeTaskRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var timerEngine: TimerEngine

    @Before
    fun setup() {
        testClock = TestClock()
        fakeSessionRepo = FakeSessionRepository()
        fakeTaskRepo = FakeTaskRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakeSettingsRepo = FakeSettingsRepository()
    }

    @Test
    fun testStartSessionInitializesCorrectly() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        timerEngine = TimerEngine(
            clock = testClock,
            sessionRepository = fakeSessionRepo,
            taskRepository = fakeTaskRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepo
        ).apply {
            dispatcher = testDispatcher
            enableTicker = false
        }

        timerEngine.startSession(
            taskName = "Writing Thesis",
            categoryId = "cat_writing",
            goal = "Finish chapter 2",
            durationMinutes = 25,
            plannedSessionCount = 4
        )

        val state = timerEngine.state.value
        assertEquals("Writing Thesis", state.taskName)
        assertEquals(25 * 60, state.plannedDurationSec)
        assertEquals(25 * 60, state.remainingSec)
        assertEquals(0, state.overtimeSec)
        assertEquals(TimerStatus.RUNNING, state.status)
        assertEquals("Finish chapter 2", state.goal)
        assertEquals(4, state.plannedSessionCount)
    }

    @Test
    fun testPauseAndResumePreservesFocusTime() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        timerEngine = TimerEngine(
            clock = testClock,
            sessionRepository = fakeSessionRepo,
            taskRepository = fakeTaskRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepo
        ).apply {
            dispatcher = testDispatcher
            enableTicker = false
        }

        timerEngine.startSession("Coding", "cat_coding", null, 25, null)

        // Run 5 minutes (300 sec)
        testClock.advanceTimeSeconds(300)
        timerEngine.updateTick()

        assertEquals(20 * 60, timerEngine.state.value.remainingSec)

        // Pause
        timerEngine.pauseSession()
        assertEquals(TimerStatus.PAUSED, timerEngine.state.value.status)
        assertEquals(300, timerEngine.state.value.accumulatedFocusedSec)

        // Time elapses while paused (10 min)
        testClock.advanceTimeSeconds(600)
        timerEngine.updateTick()

        // Still 300 sec focused and 20 min remaining
        assertEquals(20 * 60, timerEngine.state.value.remainingSec)
        assertEquals(300, timerEngine.state.value.accumulatedFocusedSec)

        // Resume
        timerEngine.resumeSession()
        assertEquals(TimerStatus.RUNNING, timerEngine.state.value.status)

        // Run another 5 minutes
        testClock.advanceTimeSeconds(300)
        timerEngine.updateTick()

        assertEquals(15 * 60, timerEngine.state.value.remainingSec)
    }

    @Test
    fun testOvertimeCountsUpAfterCountdownExpires() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        timerEngine = TimerEngine(
            clock = testClock,
            sessionRepository = fakeSessionRepo,
            taskRepository = fakeTaskRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepo
        ).apply {
            dispatcher = testDispatcher
            enableTicker = false
        }

        timerEngine.startSession("Writing", "cat_writing", null, 25, null)

        // Advance past 25 minutes
        testClock.advanceTimeSeconds(25 * 60)
        timerEngine.updateTick()

        assertEquals(TimerStatus.OVERTIME, timerEngine.state.value.status)
        assertEquals(0, timerEngine.state.value.remainingSec)
        assertEquals(1.0f, timerEngine.state.value.progress, 0.001f)

        // Overtime for 10 minutes
        testClock.advanceTimeSeconds(600)
        timerEngine.updateTick()

        assertEquals(600, timerEngine.state.value.overtimeSec)
        assertEquals((25 * 60) + 600, timerEngine.state.value.totalFocusedSec)

        // Finish from overtime
        timerEngine.finishSession()
        assertEquals(TimerStatus.COMPLETED, timerEngine.state.value.status)

        val saved = fakeSessionRepo.sessions.firstOrNull()
        assertNotNull(saved)
        assertEquals(SessionStatus.COMPLETED, saved?.status)
        assertEquals((25 * 60) + 600, saved?.focusedSec)
        assertEquals(600, saved?.overtimeSec)
    }

    @Test
    fun testResetUnderOneMinuteDiscardsSession() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        timerEngine = TimerEngine(
            clock = testClock,
            sessionRepository = fakeSessionRepo,
            taskRepository = fakeTaskRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepo
        ).apply {
            dispatcher = testDispatcher
            enableTicker = false
        }

        timerEngine.startSession("Quick test", "cat_work", null, 25, null)

        testClock.advanceTimeSeconds(45) // only 45s
        timerEngine.updateTick()

        val loggedAsBroken = timerEngine.resetSession()
        assertFalse(loggedAsBroken)
        assertTrue(fakeSessionRepo.sessions.isEmpty())
        assertEquals(TimerStatus.IDLE, timerEngine.state.value.status)
    }

    @Test
    fun testResetOverOneMinuteLogsAsBrokenWithPartialMinutes() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        timerEngine = TimerEngine(
            clock = testClock,
            sessionRepository = fakeSessionRepo,
            taskRepository = fakeTaskRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepo
        ).apply {
            dispatcher = testDispatcher
            enableTicker = false
        }

        timerEngine.startSession("Long test", "cat_work", null, 25, null)

        testClock.advanceTimeSeconds(180) // 3 minutes
        timerEngine.updateTick()

        val loggedAsBroken = timerEngine.resetSession()
        assertTrue(loggedAsBroken)

        val saved = fakeSessionRepo.sessions.firstOrNull()
        assertNotNull(saved)
        assertEquals(SessionStatus.BROKEN, saved?.status)
        assertEquals(180, saved?.focusedSec)
        assertEquals(TimerStatus.IDLE, timerEngine.state.value.status)
    }

    @Test
    fun testRebootReconciliationMarksActiveSessionAsBroken() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        timerEngine = TimerEngine(
            clock = testClock,
            sessionRepository = fakeSessionRepo,
            taskRepository = fakeTaskRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepo
        ).apply {
            dispatcher = testDispatcher
            enableTicker = false
        }

        timerEngine.startSession("Deep study", "cat_study", null, 30, null)
        testClock.advanceTimeSeconds(600) // 10 minutes
        timerEngine.updateTick()

        // Simulate reboot reconciliation
        timerEngine.reconcileOnStartup(rebootDetected = true)

        assertEquals(TimerStatus.BROKEN, timerEngine.state.value.status)
        val saved = fakeSessionRepo.sessions.firstOrNull()
        assertNotNull(saved)
        assertEquals(SessionStatus.BROKEN, saved?.status)
        assertEquals(600, saved?.focusedSec)
    }
}
