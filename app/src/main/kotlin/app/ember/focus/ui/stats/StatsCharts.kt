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
    val dayOfWeekLabel: String, // "Mon", "Tue", "Wed"...
    val minutes: Int,
    val isToday: Boolean = false
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
                    val gutterWidth = 56.dp.toPx()
                    val barAreaWidth = width - gutterWidth
                    if (offset.x <= barAreaWidth) {
                        val colWidth = barAreaWidth / 7f
                        val idx = (offset.x / colWidth).toInt().coerceIn(0, 6)
                        selectedIndex = if (selectedIndex == idx) -1 else idx
                        onDaySelected(if (selectedIndex != -1 && selectedIndex < dayStats.size) dayStats[selectedIndex] else null)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val width = size.width
            val height = size.height

            val gutterWidth = 56.dp.toPx()
            val barAreaWidth = width - gutterWidth

            val bottomPadding = 30.dp.toPx()
            val topPadding = 24.dp.toPx()
            val chartHeight = height - bottomPadding - topPadding

            val colWidth = barAreaWidth / 7f
            val barWidth = (colWidth * 0.44f).coerceAtLeast(12.dp.toPx())

            // 1. Goal Dashed Line across the bar area at 40% alpha (BEHIND the bars)
            val goalY = topPadding + chartHeight * (1f - (dailyGoalMinutes / maxMinutes).coerceIn(0f, 1f))
            drawLine(
                color = AmberAccent.copy(alpha = 0.4f),
                start = Offset(0f, goalY),
                end = Offset(barAreaWidth, goalY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )

            // 2. Bars for each day of the week
            val hasAnyData = dayStats.any { it.minutes > 0 }

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

                if (stat.isToday) {
                    // Today highlight ring around slot
                    drawRoundRect(
                        color = AmberAccent.copy(alpha = 0.35f),
                        topLeft = Offset(barLeft - 1.dp.toPx(), topPadding - 1.dp.toPx()),
                        size = Size(barWidth + 2.dp.toPx(), chartHeight + 2.dp.toPx()),
                        cornerRadius = CornerRadius((barWidth + 2.dp.toPx()) * 0.5f, (barWidth + 2.dp.toPx()) * 0.5f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Actual focus time bar
                if (stat.minutes > 0) {
                    val barBrush = Brush.verticalGradient(
                        colors = if (stat.isToday) {
                            listOf(Color(0xFFFFC078), AmberAccent)
                        } else {
                            listOf(Color(0xFFFFB74D), AmberAccent)
                        },
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

                    // Value label above non-empty bar (ST2)
                    val h = stat.minutes / 60
                    val m = stat.minutes % 60
                    val valText = if (h > 0 && m > 0) "${h}h${m}m" else if (h > 0) "${h}h" else "${m}m"

                    drawContext.canvas.nativeCanvas.drawText(
                        valText,
                        colCenterX,
                        (barTop - 5.dp.toPx()).coerceAtLeast(13.dp.toPx()),
                        android.graphics.Paint().apply {
                            color = if (stat.isToday || isSelected) "#F2E6D0".toColorInt() else "#B9AD9A".toColorInt()
                            textSize = 10.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }

                // Day Label below (ST2: Mon Tue Wed Thu Fri Sat Sun, 11sp, highlight today in amber)
                val labelColor = when {
                    stat.isToday -> "#E8A15A".toColorInt()
                    isSelected -> "#F2E6D0".toColorInt()
                    else -> "#8C8176".toColorInt()
                }

                drawContext.canvas.nativeCanvas.drawText(
                    stat.dayOfWeekLabel,
                    colCenterX,
                    height - 8.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = labelColor
                        textSize = 11.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = stat.isToday || isSelected
                        isAntiAlias = true
                    }
                )
            }

            // Empty state caption (ST3)
            if (!hasAnyData) {
                drawContext.canvas.nativeCanvas.drawText(
                    "No candles this week",
                    barAreaWidth * 0.5f,
                    topPadding + (chartHeight * 0.5f) + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = "#8C8176".toColorInt()
                        textSize = 13.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }

            // 3. Goal Label LAST inside gutter, right-aligned, 11sp Medium amber (ST1)
            val goalHours = dailyGoalMinutes / 60
            val goalMins = dailyGoalMinutes % 60
            val goalText = if (goalMins == 0) "${goalHours}h goal" else "${goalHours}h${goalMins}m goal"

            drawContext.canvas.nativeCanvas.drawText(
                goalText,
                width - 2.dp.toPx(),
                goalY + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = "#E8A15A".toColorInt()
                    textSize = 11.dp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
            )
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

            // Day of week headers: Mon Tue Wed Thu Fri Sat Sun
            val headers = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
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
                } else if (stat.isToday) {
                    drawRoundRect(
                        color = AmberAccent,
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
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
                } else if (stat.isToday) {
                    "#E8A15A".toColorInt()
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
