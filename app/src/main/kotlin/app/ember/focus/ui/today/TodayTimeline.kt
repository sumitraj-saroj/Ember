package app.ember.focus.ui.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.AshGray
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.TableWoodBottom
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale
import java.time.ZoneId

@Composable
fun TodayTimeline(
    sessions: List<Session>,
    selectedDate: LocalDate,
    highlightedSessionId: String?,
    onSessionTapped: (Session) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val isToday = remember(selectedDate) { selectedDate == LocalDate.now() }

    // Auto-fit range: from 1 hr before first session to 1 hr after last (min 6 hrs window)
    val (startHour, endHour) = remember(sessions) {
        if (sessions.isEmpty()) {
            Pair(8f, 18f) // Default 8:00 to 18:00 (10 hrs)
        } else {
            var minH = 24f
            var maxH = 0f
            sessions.forEach { s ->
                val startLocal = LocalDateTime.ofInstant(Instant.ofEpochMilli(s.startedAt), zoneId).toLocalTime()
                val endLocal = LocalDateTime.ofInstant(Instant.ofEpochMilli(s.endedAt), zoneId).toLocalTime()

                val sH = startLocal.hour + (startLocal.minute / 60f)
                val eH = endLocal.hour + (endLocal.minute / 60f)

                if (sH < minH) minH = sH
                if (eH > maxH) maxH = eH
            }
            val paddedStart = (minH - 1f).coerceAtLeast(0f)
            val paddedEnd = (maxH + 1f).coerceAtMost(24f)
            val diff = paddedEnd - paddedStart
            if (diff < 6f) {
                val need = 6f - diff
                val newStart = (paddedStart - need / 2f).coerceAtLeast(0f)
                val newEnd = (newStart + 6f).coerceAtMost(24f)
                Pair(newStart, newEnd)
            } else {
                Pair(paddedStart, paddedEnd)
            }
        }
    }

    val totalWindowHours = (endHour - startHour).coerceAtLeast(1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .pointerInput(sessions, startHour, endHour) {
                detectTapGestures { offset ->
                    val width = size.width
                    val tappedFraction = (offset.x / width).coerceIn(0f, 1f)
                    val tappedHour = startHour + (tappedFraction * totalWindowHours)

                    val matched = sessions.find { s ->
                        val startLocal = LocalDateTime.ofInstant(Instant.ofEpochMilli(s.startedAt), zoneId).toLocalTime()
                        val endLocal = LocalDateTime.ofInstant(Instant.ofEpochMilli(s.endedAt), zoneId).toLocalTime()
                        val sH = startLocal.hour + (startLocal.minute / 60f)
                        val eH = endLocal.hour + (endLocal.minute / 60f)
                        tappedHour in sH..eH
                    }
                    if (matched != null) {
                        onSessionTapped(matched)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(96.dp)) {
            val width = size.width
            val height = size.height

            // Timeline background strip
            val barTop = 32.dp.toPx()
            val barHeight = 40.dp.toPx()
            val barCornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())

            drawRoundRect(
                color = SecondaryPillFill,
                topLeft = Offset(0f, barTop),
                size = Size(width, barHeight),
                cornerRadius = barCornerRadius
            )

            // Hour tick markers
            val firstIntHour = kotlin.math.ceil(startHour).toInt()
            val lastIntHour = kotlin.math.floor(endHour).toInt()

            for (h in firstIntHour..lastIntHour) {
                val hourFraction = (h - startHour) / totalWindowHours
                val x = hourFraction * width

                // Tick line
                drawLine(
                    color = SecondaryPillBorder,
                    start = Offset(x, barTop),
                    end = Offset(x, barTop + barHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // Hour label
                val text = String.format(Locale.US, "%02d:00", h)
                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    x,
                    22.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = "#8C8176".toColorInt()
                        textSize = 10.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }

            // Draw session blocks
            sessions.forEach { s ->
                val startLocal = Instant.ofEpochMilli(s.startedAt).atZone(zoneId).toLocalTime()
                val sH = startLocal.hour + (startLocal.minute / 60f)
                val durationHours = (s.focusedSec / 3600f).coerceAtLeast(0.08f)

                val blockStartX = ((sH - startHour) / totalWindowHours) * width
                val blockWidth = (durationHours / totalWindowHours) * width
                val isHighlighted = s.id == highlightedSessionId

                val blockColor = try {
                    Color(s.categoryColorHex.toColorInt())
                } catch (_: Exception) {
                    AmberAccent
                }

                if (s.status == SessionStatus.COMPLETED) {
                    // Completed block: category color fill
                    drawRoundRect(
                        color = blockColor.copy(alpha = if (isHighlighted) 1.0f else 0.85f),
                        topLeft = Offset(blockStartX, barTop + 4.dp.toPx()),
                        size = Size(blockWidth.coerceAtLeast(8.dp.toPx()), barHeight - 8.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    if (isHighlighted) {
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(blockStartX, barTop + 4.dp.toPx()),
                            size = Size(blockWidth.coerceAtLeast(8.dp.toPx()), barHeight - 8.dp.toPx()),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                } else {
                    // Broken block: ash gray fill with dashed outline
                    drawRoundRect(
                        color = AshGray.copy(alpha = 0.35f),
                        topLeft = Offset(blockStartX, barTop + 4.dp.toPx()),
                        size = Size(blockWidth.coerceAtLeast(8.dp.toPx()), barHeight - 8.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    drawRoundRect(
                        color = AshGray,
                        topLeft = Offset(blockStartX, barTop + 4.dp.toPx()),
                        size = Size(blockWidth.coerceAtLeast(8.dp.toPx()), barHeight - 8.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                }
            }

            // "NOW" line marker on today
            if (isToday) {
                val now = LocalTime.now()
                val nowH = now.hour + (now.minute / 60f)
                if (nowH in startHour..endHour) {
                    val nowX = ((nowH - startHour) / totalWindowHours) * width

                    // Glowing amber now indicator line
                    drawLine(
                        color = AmberAccent,
                        start = Offset(nowX, barTop - 4.dp.toPx()),
                        end = Offset(nowX, barTop + barHeight + 4.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Small diamond/circle head on top
                    drawCircle(
                        color = AmberAccent,
                        center = Offset(nowX, barTop - 4.dp.toPx()),
                        radius = 3.dp.toPx()
                    )
                }
            }
        }
    }
}
