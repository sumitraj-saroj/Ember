package app.ember.focus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.domain.model.Task
import app.ember.focus.ui.candle.CandlePhase
import app.ember.focus.ui.candle.CandleState
import app.ember.focus.ui.candle.CandleView
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.today.SessionCard
import app.ember.focus.ui.today.StartSessionSheet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
class ComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testStartSessionSheetRendersAndTriggersStart() {
        var startedTaskName = ""
        var startedDuration = 0

        val categories = listOf(
            Category("cat_work", "Work", "#E8A15A"),
            Category("cat_coding", "Coding", "#F4A261")
        )

        composeTestRule.setContent {
            EmberTheme {
                StartSessionSheet(
                    recentTasks = emptyList(),
                    categories = categories,
                    defaultDurationMinutes = 25,
                    onDismiss = {},
                    onAddCategory = { _, _ -> },
                    onStartSession = { task, _, _, duration, _ ->
                        startedTaskName = task
                        startedDuration = duration
                    }
                )
            }
        }

        // Verify elements are displayed
        composeTestRule.onNodeWithText("LIGHT A CANDLE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Begin Deep Focus").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coding").assertIsDisplayed()

        // Enter task name
        composeTestRule.onNodeWithText("e.g. Write chapter draft, Design auth flow")
            .performTextInput("Write Core Tests")

        // Tap "Light the candle"
        composeTestRule.onNodeWithText("Light the candle").performClick()

        assertEquals("Write Core Tests", startedTaskName)
        assertEquals(25, startedDuration)
    }

    @Test
    fun testSessionCardDisplaysDetailsAndHandlesClick() {
        var clicked = false
        val session = Session(
            id = "sess_1",
            taskId = "task_1",
            taskName = "Design Motion Engine",
            categoryId = "cat_work",
            categoryName = "Work",
            categoryColorHex = "#E8A15A",
            goal = "60fps render loop",
            plannedDurationSec = 25 * 60,
            focusedSec = 30 * 60,
            overtimeSec = 5 * 60,
            startedAt = 1700000000000L,
            endedAt = 1700001800000L,
            timeZoneId = "UTC",
            status = SessionStatus.COMPLETED,
            rating = 5
        )

        composeTestRule.setContent {
            EmberTheme {
                SessionCard(
                    session = session,
                    isHighlighted = false,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Design Motion Engine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Goal: 60fps render loop").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 min (+5m)").assertIsDisplayed()

        composeTestRule.onNodeWithText("Design Motion Engine").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testCandleViewRendersAcrossPhases() {
        // Disable autoAdvance to handle rememberInfiniteTransition without idling timeout
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            EmberTheme {
                CandleView(
                    state = CandleState(
                        progress = 0.5f,
                        phase = CandlePhase.BURNING
                    )
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(100)

        // Check TalkBack accessibility text
        composeTestRule.onNodeWithContentDescription("Candle 50% burned, flame glowing")
            .assertExists()
    }
}
