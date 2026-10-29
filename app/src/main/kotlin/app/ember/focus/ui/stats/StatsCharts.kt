package app.ember.focus.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.AshGray
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import java.time.LocalDate

data class DayStat(
    val date: LocalDate,
    val dayOfWeekLabel: String, // "M", "T", "W"...
    val minutes: Int
)

@Composable
fun WeekBarChart(
    dayStats: List<DayStat>,
    dailyGoalMinutes: Int,
    onDaySelected: (DayStat?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val maxMinutes = remember(dayStats, dailyGoalMinutes) {
        val peak = dayStats.maxOfOrNull { it.minutes } ?: 0
        maxOf(peak, dailyGoalMinutes, 60).toFloat() * 1.15f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(dayStats) {
                detectTapGestures { offset ->
                    val width = size.width
                    val colWidth = width / 7f
                    val idx = (offset.x / colWidth).toInt().coerceIn(0, 6)
                    selectedIndex = if (selectedIndex == idx) -1 else idx
                    onDaySelected(if (selectedIndex != -1 && selectedIndex < dayStats.size) dayStats[selectedIndex] else null)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val width = size.width
            val height = size.height

            val bottomPadding = 32.dp.toPx()
            val topPadding = 24.dp.toPx()
            val chartHeight = height - bottomPadding - topPadding

            val colWidth = width / 7f
            val barWidth = colWidth * 0.48f

            // 1. Goal Dashed Line
            val goalY = topPadding + chartHeight * (1f - (dailyGoalMinutes / maxMinutes).coerceIn(0f, 1f))
            drawLine(
                color = AmberAccent.copy(alpha = 0.5f),
                start = Offset(0f, goalY),
                end = Offset(width, goalY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            // Goal Label
            val goalHours = dailyGoalMinutes / 60
            drawContext.canvas.nativeCanvas.drawText(
                "${goalHours}h goal",
                width - 4.dp.toPx(),
                goalY - 6.dp.toPx(),
                android.graphics.Paint().apply {
                    color = "#E8A15A".toColorInt()
                    textSize = 10.dp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
            )

            // 2. Bars for each day of the week
            dayStats.forEachIndexed { i, stat ->
                val colCenterX = (i * colWidth) + (colWidth * 0.5f)
                val barLeft = colCenterX - (barWidth * 0.5f)

                val barHeight = chartHeight * (stat.minutes / maxMinutes).coerceIn(0f, 1f)
                val barTop = topPadding + chartHeight - barHeight

                val isSelected = i == selectedIndex

                // Background track slot
                drawRoundRect(
                    color = SecondaryPillFill,
                    topLeft = Offset(barLeft, topPadding),
                    size = Size(barWidth, chartHeight),
                    cornerRadius = CornerRadius(barWidth * 0.5f, barWidth * 0.5f)
                )

                // Actual focus time bar
                if (stat.minutes > 0) {
                    val barBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFB74D),
                            AmberAccent
                        ),
                        startY = barTop,
                        endY = topPadding + chartHeight
                    )

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth * 0.5f, barWidth * 0.5f)
                    )

                    if (isSelected) {
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(barLeft - 2.dp.toPx(), barTop - 2.dp.toPx()),
                            size = Size(barWidth + 4.dp.toPx(), barHeight + 4.dp.toPx()),
                            cornerRadius = CornerRadius(barWidth * 0.5f, barWidth * 0.5f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Day Label below
                drawContext.canvas.nativeCanvas.drawText(
                    stat.dayOfWeekLabel,
                    colCenterX,
                    height - 10.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = if (isSelected) "#F2E6D0".toColorInt() else "#8C8176".toColorInt()
                        textSize = 12.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isSelected
                        isAntiAlias = true
                    }
                )

                // Tooltip bubble on selected bar
                if (isSelected && stat.minutes > 0) {
                    val h = stat.minutes / 60
                    val m = stat.minutes % 60
                    val text = if (h > 0) "${h}h ${m}m" else "${m}m"

                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        colCenterX,
                        (barTop - 8.dp.toPx()).coerceAtLeast(14.dp.toPx()),
                        android.graphics.Paint().apply {
                            color = "#F2E6D0".toColorInt()
                            textSize = 11.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MonthHeatmap(
    daysInMonth: List<DayStat>,
    dailyGoalMinutes: Int,
    onDayTapped: (DayStat) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .pointerInput(daysInMonth) {
                detectTapGestures { offset ->
                    val width = size.width
                    val height = size.height
                    val colWidth = width / 7f
                    val totalDays = daysInMonth.size
                    if (totalDays == 0) return@detectTapGestures

                    val firstDayOffset = daysInMonth.first().date.dayOfWeek.value - 1 // Mon=0
                    val rowHeight = (height - 24.dp.toPx()) / 6f

                    val clickedCol = (offset.x / colWidth).toInt().coerceIn(0, 6)
                    val clickedRow = ((offset.y - 24.dp.toPx()) / rowHeight).toInt().coerceIn(0, 5)

                    val slotIndex = clickedRow * 7 + clickedCol
                    val dayIndex = slotIndex - firstDayOffset

                    if (dayIndex in daysInMonth.indices) {
                        val stat = daysInMonth[dayIndex]
                        selectedDate = stat.date
                        onDayTapped(stat)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            val width = size.width
            val height = size.height

            val colWidth = width / 7f
            val headerHeight = 22.dp.toPx()
            val gridTop = headerHeight
            val rowHeight = (height - gridTop) / 6f
            val cellPadding = 3.dp.toPx()
            val cellSize = (minOf(colWidth, rowHeight) - cellPadding * 2f).coerceAtLeast(10f)

            // Day of week headers: M T W T F S S
            val headers = listOf("M", "T", "W", "T", "F", "S", "S")
            headers.forEachIndexed { i, h ->
                val x = (i * colWidth) + (colWidth * 0.5f)
                drawContext.canvas.nativeCanvas.drawText(
                    h,
                    x,
                    14.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = "#8C8176".toColorInt()
                        textSize = 10.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }

            if (daysInMonth.isEmpty()) return@Canvas
            val firstDayOffset = daysInMonth.first().date.dayOfWeek.value - 1

            daysInMonth.forEachIndexed { i, stat ->
                val slot = i + firstDayOffset
                val col = slot % 7
                val row = slot / 7

                val x = (col * colWidth) + ((colWidth - cellSize) * 0.5f)
                val y = gridTop + (row * rowHeight) + ((rowHeight - cellSize) * 0.5f)

                // Heat intensity based on daily goal
                val intensity = if (dailyGoalMinutes > 0) {
                    (stat.minutes.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val cellColor = if (stat.minutes == 0) {
                    SecondaryPillFill
                } else {
                    AmberAccent.copy(alpha = (0.25f + (intensity * 0.75f)).coerceIn(0.25f, 1f))
                }

                val isSelected = stat.date == selectedDate

                drawRoundRect(
                    color = cellColor,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                if (isSelected) {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                } else if (stat.minutes == 0) {
                    drawRoundRect(
                        color = SecondaryPillBorder,
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 0.8.dp.toPx())
                    )
                }

                // Day number text
                val textColor = if (stat.minutes >= dailyGoalMinutes) {
                    "#1A120C".toColorInt()
                } else {
                    "#F2E6D0".toColorInt()
                }

                drawContext.canvas.nativeCanvas.drawText(
                    stat.date.dayOfMonth.toString(),
                    x + (cellSize * 0.5f),
                    y + (cellSize * 0.65f),
                    android.graphics.Paint().apply {
                        color = textColor
                        textSize = (cellSize * 0.42f).coerceAtMost(11.dp.toPx())
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}
