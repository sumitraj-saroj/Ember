package app.ember.focus.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.ChipUnselectedBorder
import app.ember.focus.ui.theme.ChipUnselectedFill
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.SwitchOffBorder
import app.ember.focus.ui.theme.SwitchOffThumb
import app.ember.focus.ui.theme.SwitchOffTrack
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextDark
import app.ember.focus.ui.theme.TextMuted
import app.ember.focus.ui.theme.TrackInactive

// =========================================================================
// G2. Number + Unit Styling (DurationText, StreakText, UnitValueText)
// =========================================================================

/**
 * Builds an AnnotatedString where numbers and units have distinct font styles
 * with perfect baseline alignment and thin-space separation.
 * e.g., "3h 25m" (big 3, small h, thin space, big 25, small m) or "0m".
 */
@Composable
fun DurationText(
    minutes: Int,
    modifier: Modifier = Modifier,
    numberStyle: TextStyle = EmberTheme.typography.heroTotalNumber,
    unitStyle: TextStyle = EmberTheme.typography.heroTotalUnit
) {
    val annotatedString = remember(minutes, numberStyle, unitStyle) {
        buildAnnotatedString {
            val h = minutes / 60
            val m = minutes % 60
            val thinSpace = "\u2009"

            if (h > 0) {
                withStyle(numberStyle.toSpanStyle()) {
                    append(h.toString())
                }
                withStyle(unitStyle.toSpanStyle()) {
                    append("h")
                }
                if (m > 0) {
                    append(thinSpace)
                    withStyle(numberStyle.toSpanStyle()) {
                        append(m.toString())
                    }
                    withStyle(unitStyle.toSpanStyle()) {
                        append("m")
                    }
                }
            } else {
                withStyle(numberStyle.toSpanStyle()) {
                    append(m.toString())
                }
                withStyle(unitStyle.toSpanStyle()) {
                    append("m")
                }
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
    )
}

/**
 * Formats a streak number + unit, e.g. "0 days", "1 day", "5 days".
 */
@Composable
fun StreakText(
    days: Int,
    modifier: Modifier = Modifier,
    numberStyle: TextStyle = EmberTheme.typography.heroTotalNumber,
    unitStyle: TextStyle = EmberTheme.typography.heroTotalUnit
) {
    val annotatedString = remember(days, numberStyle, unitStyle) {
        buildAnnotatedString {
            withStyle(numberStyle.toSpanStyle()) {
                append(days.toString())
            }
            append("\u2009")
            withStyle(unitStyle.toSpanStyle()) {
                append(if (days == 1) "day" else "days")
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
    )
}

/**
 * General purpose number + unit text with baseline alignment.
 */
@Composable
fun UnitValueText(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    numberStyle: TextStyle = EmberTheme.typography.statCardValue,
    unitStyle: TextStyle = EmberTheme.typography.statCardUnit
) {
    val annotatedString = remember(value, unit, numberStyle, unitStyle) {
        buildAnnotatedString {
            withStyle(numberStyle.toSpanStyle()) {
                append(value)
            }
            if (unit.isNotEmpty()) {
                append("\u2009")
                withStyle(unitStyle.toSpanStyle()) {
                    append(unit)
                }
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
    )
}

// =========================================================================
// G3. Scroll Scrim Overlays (Content scrolls under transparent system bars)
// =========================================================================

@Composable
fun TopFadeScrim(
    modifier: Modifier = Modifier,
    extraHeight: Dp = 28.dp
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val totalHeight = statusBarHeight + extraHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundTop,
                        BackgroundTop.copy(alpha = 0.85f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun BottomFadeScrim(
    modifier: Modifier = Modifier,
    extraHeight: Dp = 28.dp
) {
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val totalHeight = navBarHeight + extraHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        BackgroundBottom.copy(alpha = 0.85f),
                        BackgroundBottom
                    )
                )
            )
    )
}

// =========================================================================
// G5. Switch (Toggle) with High Contrast States
// =========================================================================

@Composable
fun EmberSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val trackWidth = 50.dp
    val trackHeight = 30.dp
    val thumbSize = 22.dp
    val padding = 4.dp

    val maxOffset = trackWidth - thumbSize - (padding * 2)
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) maxOffset else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )

    val trackColor = if (checked) AmberAccent else SwitchOffTrack
    val thumbColor = if (checked) TextDark else SwitchOffThumb
    val borderStroke = if (checked) null else BorderStroke(1.5.dp, SwitchOffBorder)

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(15.dp))
            .background(trackColor)
            .then(
                if (borderStroke != null) {
                    Modifier.border(borderStroke, RoundedCornerShape(15.dp))
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(padding)
            .semantics {
                role = Role.Switch
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

// =========================================================================
// G6. Unified Chip Component (EmberChip & EmberDashedChip)
// =========================================================================

/**
 * Standard EmberChip:
 * - Height 40dp, fully rounded (20dp corner radius), 16dp horizontal padding.
 * - Single-choice presets (duration, break): Solid amber fill with dark text.
 * - Category chip: 22% alpha background fill, 1.5dp category border, cream text.
 * - Unselected: #1C1815 fill, 1dp #2A2520 border, cream text.
 */
@Composable
fun EmberChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryColor: Color? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(20.dp)

    val backgroundColor: Color
    val contentColor: Color
    val border: BorderStroke?

    if (selected) {
        if (categoryColor != null) {
            // Selected category chip
            backgroundColor = categoryColor.copy(alpha = 0.22f)
            contentColor = TextCream
            border = BorderStroke(1.5.dp, categoryColor)
        } else {
            // Selected single-choice preset
            backgroundColor = AmberAccent
            contentColor = TextDark
            border = null
        }
    } else {
        // Unselected chip
        backgroundColor = ChipUnselectedFill
        contentColor = TextCream
        border = BorderStroke(1.dp, ChipUnselectedBorder)
    }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(backgroundColor)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = EmberTheme.typography.button.copy(
                    fontSize = 14.sp,
                    fontWeight = if (selected && categoryColor == null) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Dashed outline chip for "+ New" category creation.
 */
@Composable
fun EmberDashedChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(ChipUnselectedFill)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = AmberAccent.copy(alpha = 0.6f),
                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )
        }
        Text(
            text = text,
            style = EmberTheme.typography.button.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            color = AmberAccent,
            maxLines = 1,
            softWrap = false
        )
    }
}

// =========================================================================
// G7. Custom Slider Component (EmberSlider)
// =========================================================================

/**
 * Custom Ember slider:
 * - 6dp track (active amber #E8A15A, inactive #2A241F).
 * - Thumb: 20dp cream circle (#F2E6D0) with 2dp amber ring (#E8A15A).
 * - Optional hour tick marks (subtle small dots) on inactive part for Daily Goal.
 * - 48dp minimum touch target height.
 */
@Composable
fun EmberSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    showHourTicks: Boolean = false,
    contentDescription: String = "Slider"
) {
    val trackHeight = 6.dp
    val thumbRadius = 10.dp // 20dp diameter
    val ringStroke = 2.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(valueRange, steps) {
                    fun updateValue(x: Float, width: Float) {
                        val paddingPx = thumbRadius.toPx()
                        val availableWidth = (width - paddingPx * 2).coerceAtLeast(1f)
                        val fraction = ((x - paddingPx) / availableWidth).coerceIn(0f, 1f)
                        val rangeSize = valueRange.endInclusive - valueRange.start
                        var rawValue = valueRange.start + fraction * rangeSize
                        if (steps > 0) {
                            val stepSize = rangeSize / (steps + 1)
                            val stepIndex = ((rawValue - valueRange.start) / stepSize).toInt()
                            val lower = valueRange.start + stepIndex * stepSize
                            val upper = (lower + stepSize).coerceAtMost(valueRange.endInclusive)
                            rawValue = if (rawValue - lower < upper - rawValue) lower else upper
                        }
                        onValueChange(rawValue)
                    }

                    detectTapGestures { offset ->
                        updateValue(offset.x, size.width.toFloat())
                    }
                }
                .pointerInput(valueRange, steps) {
                    fun updateValue(x: Float, width: Float) {
                        val paddingPx = thumbRadius.toPx()
                        val availableWidth = (width - paddingPx * 2).coerceAtLeast(1f)
                        val fraction = ((x - paddingPx) / availableWidth).coerceIn(0f, 1f)
                        val rangeSize = valueRange.endInclusive - valueRange.start
                        var rawValue = valueRange.start + fraction * rangeSize
                        if (steps > 0) {
                            val stepSize = rangeSize / (steps + 1)
                            val stepIndex = Math.round((rawValue - valueRange.start) / stepSize)
                            rawValue = (valueRange.start + stepIndex * stepSize).coerceIn(valueRange.start, valueRange.endInclusive)
                        }
                        onValueChange(rawValue)
                    }

                    detectDragGestures { change, _ ->
                        change.consume()
                        updateValue(change.position.x, size.width.toFloat())
                    }
                }
        ) {
            val width = size.width
            val centerY = size.height * 0.5f
            val thumbRadiusPx = thumbRadius.toPx()
            val availableWidth = width - thumbRadiusPx * 2
            val rangeSize = valueRange.endInclusive - valueRange.start
            val fraction = if (rangeSize > 0f) {
                ((value - valueRange.start) / rangeSize).coerceIn(0f, 1f)
            } else 0f

            val trackHeightPx = trackHeight.toPx()
            val trackTop = centerY - trackHeightPx * 0.5f
            val thumbX = thumbRadiusPx + fraction * availableWidth

            // Inactive track (full width background)
            drawRoundRect(
                color = TrackInactive,
                topLeft = Offset(thumbRadiusPx, trackTop),
                size = Size(availableWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx * 0.5f, trackHeightPx * 0.5f)
            )

            // Hour tick marks on inactive part
            if (showHourTicks && steps > 0) {
                for (i in 1..steps) {
                    val tickFraction = i.toFloat() / (steps + 1).toFloat()
                    val tickX = thumbRadiusPx + tickFraction * availableWidth
                    // Only draw tick if on inactive portion (past thumb)
                    if (tickX > thumbX + 6f) {
                        drawCircle(
                            color = Color(0xFF5A5047),
                            radius = 2.dp.toPx(),
                            center = Offset(tickX, centerY)
                        )
                    }
                }
            }

            // Active track (from start to thumb position)
            if (thumbX > thumbRadiusPx) {
                drawRoundRect(
                    color = AmberAccent,
                    topLeft = Offset(thumbRadiusPx, trackTop),
                    size = Size(thumbX - thumbRadiusPx, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx * 0.5f, trackHeightPx * 0.5f)
                )
            }

            // Thumb: Cream circle with amber ring
            drawCircle(
                color = TextCream,
                radius = thumbRadiusPx,
                center = Offset(thumbX, centerY)
            )
            drawCircle(
                color = AmberAccent,
                radius = thumbRadiusPx - 1.dp.toPx(),
                center = Offset(thumbX, centerY),
                style = Stroke(width = ringStroke.toPx())
            )
        }
    }
}
