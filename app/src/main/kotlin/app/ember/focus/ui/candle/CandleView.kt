package app.ember.focus.ui.candle

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.ember.focus.ui.theme.AshGray
import app.ember.focus.ui.theme.BrassDish
import app.ember.focus.ui.theme.BrassDishShadow
import app.ember.focus.ui.theme.TableWoodBottom
import app.ember.focus.ui.theme.TableWoodTop
import app.ember.focus.ui.theme.WaxDarkCool
import app.ember.focus.ui.theme.WaxIvory
import app.ember.focus.ui.theme.WaxShadow
import app.ember.focus.ui.theme.WickColor
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CandleView(
    state: CandleState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val animationsEnabled = remember(context) { areAnimationsEnabled(context) }

    // Continuously running time value for organic natural flicker (at 60fps)
    var animationTimeSec by remember { mutableFloatStateOf(0f) }
    var phaseElapsedMs by remember(state.phase) { mutableLongStateOf(0L) }

    LaunchedEffect(animationsEnabled, state.phase) {
        if (!animationsEnabled) return@LaunchedEffect
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastNanos != 0L) {
                    val dt = (frameNanos - lastNanos) / 1_000_000_000f
                    animationTimeSec += dt
                    phaseElapsedMs += ((frameNanos - lastNanos) / 1_000_000L)
                }
                lastNanos = frameNanos
            }
        }
    }

    // Pre-calculated deterministic wax drips for this session
    val drips = remember(state.seed) { generateWaxDrips(state.seed) }

    // Smooth transition for progress
    val smoothProgress by animateFloatAsState(
        targetValue = state.progress.coerceIn(0f, 1f),
        animationSpec = if (animationsEnabled) tween(durationMillis = 600, easing = FastOutSlowInEasing) else tween(0),
        label = "smoothProgress"
    )

    // Pre-allocated reusable paths to avoid per-frame garbage collection
    val flamePath = remember { Path() }
    val smokePath = remember { Path() }
    val candleBodyPath = remember { Path() }
    val wickPath = remember { Path() }

    val accessibilityText = remember(state.progress, state.phase) {
        val pct = (state.progress * 100).toInt()
        when (state.phase) {
            CandlePhase.IDLE -> "Unlit full candle"
            CandlePhase.IGNITING -> "Candle igniting"
            CandlePhase.BURNING -> "Candle $pct% burned, flame glowing"
            CandlePhase.PAUSED -> "Candle paused with resting ember"
            CandlePhase.OVERTIME -> "Candle burned down in overtime flow"
            CandlePhase.FINISHED -> "Candle finished, flame snuffed with gentle smoke"
            CandlePhase.BROKEN -> "Candle went out, session broken at $pct%"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = accessibilityText }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            // Geometric Layout Calculations
            val candleWidth = (width * 0.28f).coerceIn(80f, 160f)
            val fullCandleHeight = (height * 0.42f).coerceIn(160f, 420f)
            val minStubHeight = fullCandleHeight * 0.18f

            // Candle height shrinks from 100% down to 18% stub
            val currentCandleHeight = minStubHeight + (fullCandleHeight - minStubHeight) * (1f - smoothProgress)

            val centerX = width * 0.5f
            val dishCenterY = height * 0.72f
            val dishRadiusX = candleWidth * 1.55f
            val dishRadiusY = candleWidth * 0.42f

            val candleBaseY = dishCenterY - (dishRadiusY * 0.25f)
            val candleTopY = candleBaseY - currentCandleHeight
            val candleLeft = centerX - (candleWidth * 0.5f)
            val candleRight = centerX + (candleWidth * 0.5f)
            val waxPoolHeight = candleWidth * 0.26f

            // Flicker calculations using smooth sum of sines
            val flicker = if (animationsEnabled && (state.phase == CandlePhase.BURNING || state.phase == CandlePhase.OVERTIME || state.phase == CandlePhase.IGNITING)) {
                val s1 = sin(animationTimeSec * 7.5f)
                val s2 = sin(animationTimeSec * 16.3f + 1.2f) * 0.45f
                val s3 = sin(animationTimeSec * 29.8f + 2.7f) * 0.2f
                ((s1 + s2 + s3) / 1.65f).coerceIn(-1f, 1f)
            } else {
                0f
            }

            // Flame & Glow Scale Factors by Phase
            val flameScale = when (state.phase) {
                CandlePhase.IDLE -> 0f
                CandlePhase.IGNITING -> {
                    val t = (phaseElapsedMs / 1000f).coerceIn(0f, 1f)
                    t * t // quadratic ease-in
                }
                CandlePhase.BURNING -> 1f + flicker * 0.08f
                CandlePhase.PAUSED -> 0.26f // small resting ember
                CandlePhase.OVERTIME -> 0.75f + flicker * 0.06f
                CandlePhase.FINISHED -> {
                    val t = (phaseElapsedMs / 350f).coerceIn(0f, 1f)
                    (1f - t).coerceAtLeast(0f)
                }
                CandlePhase.BROKEN -> {
                    val t = (phaseElapsedMs / 250f).coerceIn(0f, 1f)
                    (1f - t).coerceAtLeast(0f)
                }
            }

            val glowRadius = candleWidth * 2.2f * flameScale * (1f + flicker * 0.08f)
            val flameBaseY = candleTopY + (waxPoolHeight * 0.35f) - 22f

            // 1. BACKGROUND WARM RADIAL GLOW (C4: BlendMode.Plus, 6+ stops)
            if (glowRadius > 10f && flameScale > 0.02f) {
                val glowAlpha = (0.35f + flicker * 0.05f).coerceIn(0.15f, 0.45f) * flameScale
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFFF9A3C).copy(alpha = glowAlpha),
                            0.2f to Color(0xFFFF8F1F).copy(alpha = glowAlpha * 0.75f),
                            0.4f to Color(0xFFF57C00).copy(alpha = glowAlpha * 0.45f),
                            0.6f to Color(0xFFE65100).copy(alpha = glowAlpha * 0.22f),
                            0.8f to Color(0xFFBF360C).copy(alpha = glowAlpha * 0.08f),
                            1.0f to Color(0x00000000)
                        ),
                        center = Offset(centerX, flameBaseY - 20f),
                        radius = glowRadius
                    ),
                    center = Offset(centerX, flameBaseY - 20f),
                    radius = glowRadius,
                    blendMode = BlendMode.Plus
                )
            }

            // 2. SURFACE / TABLE FLOOR (C5: No hard edges, full-width gradient + vignette + light pool)
            val floorTop = dishCenterY - (dishRadiusY * 0.5f)
            val floorHeight = height - floorTop
            if (floorHeight > 0f) {
                // Vertical floor gradient: transparent (above dish) -> #17110C -> #0D0906
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.25f to Color(0xFF17110C),
                            1.0f to Color(0xFF0D0906)
                        ),
                        startY = floorTop,
                        endY = height
                    ),
                    topLeft = Offset(0f, floorTop),
                    size = Size(width, floorHeight)
                )

                // Horizontal vignette so it fades softly at left and right edges
                drawRect(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x990A0705),
                            0.2f to Color.Transparent,
                            0.8f to Color.Transparent,
                            1.0f to Color(0x990A0705)
                        ),
                        startX = 0f,
                        endX = width
                    ),
                    topLeft = Offset(0f, floorTop),
                    size = Size(width, floorHeight)
                )

                // Soft contact-shadow ellipse under the brass dish
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x99000000),
                            Color(0x44000000),
                            Color(0x00000000)
                        ),
                        center = Offset(centerX, dishCenterY + 4f),
                        radius = dishRadiusX * 1.3f
                    ),
                    topLeft = Offset(centerX - dishRadiusX * 1.3f, dishCenterY - dishRadiusY * 0.4f),
                    size = Size(dishRadiusX * 2.6f, dishRadiusY * 2.2f)
                )

                // Warm elliptical light pool on the surface whose alpha follows flame intensity
                if (flameScale > 0.05f) {
                    val lightPoolAlpha = (0.16f * flameScale * (1f + flicker * 0.08f)).coerceIn(0f, 0.25f)
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF9A3C).copy(alpha = lightPoolAlpha),
                                Color(0xFFE65100).copy(alpha = lightPoolAlpha * 0.4f),
                                Color(0x00000000)
                            ),
                            center = Offset(centerX, dishCenterY + (dishRadiusY * 0.4f)),
                            radius = dishRadiusX * 2.0f
                        ),
                        topLeft = Offset(centerX - dishRadiusX * 2.0f, dishCenterY - dishRadiusY * 0.5f),
                        size = Size(dishRadiusX * 4.0f, dishRadiusY * 2.2f),
                        blendMode = BlendMode.Plus
                    )
                }
            }

            // 3. BRASS DISH (C6)
            // Dish metallic body
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BrassDish,
                        Color(0xFF8F6B2A),
                        BrassDishShadow
                    ),
                    center = Offset(centerX - (dishRadiusX * 0.2f), dishCenterY),
                    radius = dishRadiusX
                ),
                topLeft = Offset(centerX - dishRadiusX, dishCenterY - dishRadiusY),
                size = Size(dishRadiusX * 2f, dishRadiusY * 2f)
            )
            // Dish inner recess
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A3716),
                        Color(0xFF75551E)
                    ),
                    center = Offset(centerX, dishCenterY),
                    radius = dishRadiusX * 0.75f
                ),
                topLeft = Offset(centerX - dishRadiusX * 0.75f, dishCenterY - dishRadiusY * 0.72f),
                size = Size(dishRadiusX * 1.5f, dishRadiusY * 1.44f)
            )
            // Soft shadow under candle base (replacing the grey ring)
            drawOval(
                color = Color(0x662A1F14),
                topLeft = Offset(centerX - candleWidth * 0.55f, candleBaseY - 3f),
                size = Size(candleWidth * 1.1f, waxPoolHeight * 0.6f)
            )

            // 4. MELTED WAX PUDDLE AT BASE
            val puddleRadiusX = candleWidth * (0.6f + smoothProgress * 0.45f)
            val puddleRadiusY = waxPoolHeight * (0.55f + smoothProgress * 0.45f)
            val puddleTop = candleBaseY - (puddleRadiusY * 0.8f)

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFAF5EC).copy(alpha = 0.9f),
                        Color(0xFFE8DAC3),
                        Color(0xFFBFAE8E)
                    ),
                    center = Offset(centerX, candleBaseY),
                    radius = puddleRadiusX
                ),
                topLeft = Offset(centerX - puddleRadiusX, puddleTop),
                size = Size(puddleRadiusX * 2f, puddleRadiusY * 2f)
            )

            // 5. CANDLE WAX CYLINDER BODY (Warm opaque tones only, C1)
            val isBroken = state.phase == CandlePhase.BROKEN
            val edgeColor = if (isBroken) Color(0xFF7A7067) else Color(0xFFBFAE8E)
            val midColor = if (isBroken) Color(0xFF9E948A) else Color(0xFFEFE3CB)
            val highlightColor = if (isBroken) Color(0xFFB8ADA3) else Color(0xFFFBF4E4)

            candleBodyPath.reset()
            candleBodyPath.moveTo(candleLeft, candleTopY + (waxPoolHeight * 0.5f))
            candleBodyPath.lineTo(candleRight, candleTopY + (waxPoolHeight * 0.5f))
            candleBodyPath.lineTo(candleRight, candleBaseY)
            candleBodyPath.lineTo(candleLeft, candleBaseY)
            candleBodyPath.close()

            // Horizontal cylinder shading: edge #BFAE8E -> mid #EFE3CB -> highlight #FBF4E4 at ~35% -> mid -> edge
            drawPath(
                path = candleBodyPath,
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to edgeColor,
                        0.18f to midColor,
                        0.35f to highlightColor,
                        0.68f to midColor,
                        1.0f to edgeColor
                    ),
                    startX = candleLeft,
                    endX = candleRight
                )
            )

            // Bottom ambient occlusion #8F7F63 at 25% alpha
            val aoHeight = currentCandleHeight * 0.25f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF8F7F63).copy(alpha = 0.25f)
                    ),
                    startY = candleBaseY - aoHeight,
                    endY = candleBaseY
                ),
                topLeft = Offset(candleLeft, candleBaseY - aoHeight),
                size = Size(candleWidth, aoHeight)
            )

            // Top amber glow overlay on top 30% of body when burning
            if (flameScale > 0.05f) {
                val glowOverlayHeight = currentCandleHeight * 0.30f
                val glowOverlayAlpha = (0.35f * flameScale * (1f + flicker * 0.08f)).coerceIn(0f, 0.45f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF9A3C).copy(alpha = glowOverlayAlpha),
                            Color(0x00000000)
                        ),
                        startY = candleTopY + (waxPoolHeight * 0.5f),
                        endY = candleTopY + (waxPoolHeight * 0.5f) + glowOverlayHeight
                    ),
                    topLeft = Offset(candleLeft, candleTopY + (waxPoolHeight * 0.5f)),
                    size = Size(candleWidth, glowOverlayHeight),
                    blendMode = BlendMode.Plus
                )
            }

            // 6. PROCEDURAL WAX DRIPS DOWN SIDES
            drips.forEach { drip ->
                if (smoothProgress > drip.startProgress) {
                    val dripProgress = ((smoothProgress - drip.startProgress) / 0.35f).coerceIn(0f, 1f)
                    val dripLen = currentCandleHeight * drip.maxLenRatio * dripProgress
                    val dripX = candleLeft + (candleWidth * drip.xRatio)
                    val dripStartY = candleTopY + (waxPoolHeight * 0.5f)
                    val dripEndY = (dripStartY + dripLen).coerceAtMost(candleBaseY - 2f)

                    if (dripEndY > dripStartY) {
                        // Drip trail
                        drawLine(
                            color = midColor,
                            start = Offset(dripX, dripStartY),
                            end = Offset(dripX, dripEndY),
                            strokeWidth = drip.widthDp * density,
                            cap = StrokeCap.Round
                        )
                        // Droplet bead at bottom
                        drawCircle(
                            color = edgeColor,
                            center = Offset(dripX, dripEndY),
                            radius = (drip.widthDp * density * 0.7f)
                        )
                    }
                }
            }

            // 7. TOP WAX SURFACE (C2: No glass rim)
            val isBurning = flameScale > 0.05f || state.phase == CandlePhase.BURNING || state.phase == CandlePhase.OVERTIME

            if (!isBurning) {
                // Idle: flat ivory ellipse (slightly lighter than body) with wick
                drawOval(
                    color = Color(0xFFFAF5EC),
                    topLeft = Offset(candleLeft, candleTopY),
                    size = Size(candleWidth, waxPoolHeight)
                )
            } else {
                // Burning: a glossy translucent melted-wax pool (amber-tinted)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFF3E0),
                            Color(0xFFF3DEC4),
                            Color(0xFFDEBA8E)
                        ),
                        center = Offset(centerX, candleTopY + (waxPoolHeight * 0.5f)),
                        radius = candleWidth * 0.48f
                    ),
                    topLeft = Offset(candleLeft, candleTopY),
                    size = Size(candleWidth, waxPoolHeight)
                )
                // Small specular highlight inside the liquid pool
                drawOval(
                    color = Color(0x66FFFFFF),
                    topLeft = Offset(centerX - candleWidth * 0.22f, candleTopY + waxPoolHeight * 0.18f),
                    size = Size(candleWidth * 0.44f, waxPoolHeight * 0.22f)
                )
            }

            // 8. WICK
            val wickBaseX = centerX
            val wickBaseY = candleTopY + (waxPoolHeight * 0.5f)
            val wickHeight = 22f
            val wickTipX = wickBaseX - 3.5f
            val wickTipY = wickBaseY - wickHeight

            wickPath.reset()
            wickPath.moveTo(wickBaseX, wickBaseY)
            wickPath.quadraticTo(wickBaseX + 2f, wickBaseY - (wickHeight * 0.5f), wickTipX, wickTipY)

            drawPath(
                path = wickPath,
                color = WickColor,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Glowing ember tip on wick when burning or paused
            if (flameScale > 0.05f || state.phase == CandlePhase.PAUSED) {
                drawCircle(
                    color = Color(0xFFFF3D00),
                    radius = 2.5f,
                    center = Offset(wickTipX, wickTipY)
                )
            }

            // 9. THE FLAME (C3: Height ~1.2x candleWidth, width ~0.4x candleWidth, with visible layers)
            if (flameScale > 0.02f) {
                val flameHeight = candleWidth * 1.2f * flameScale * (1f + flicker * 0.06f)
                val flameWidth = candleWidth * 0.4f * flameScale * (1f - flicker * 0.04f)
                val swayX = flicker * 4.5f * flameScale

                val baseFlameX = wickTipX
                val baseFlameY = wickTipY - 2f
                val tipFlameX = baseFlameX + swayX
                val tipFlameY = baseFlameY - flameHeight

                // Faint blue base near the wick
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2979FF).copy(alpha = 0.85f * flameScale),
                            Color(0x00000000)
                        ),
                        center = Offset(baseFlameX, baseFlameY),
                        radius = flameWidth * 0.55f
                    ),
                    topLeft = Offset(baseFlameX - flameWidth * 0.55f, baseFlameY - 10f),
                    size = Size(flameWidth * 1.1f, 18f)
                )

                // Outer Mantle (Red-orange tip -> Orange mid -> Warm amber)
                flamePath.reset()
                flamePath.moveTo(baseFlameX, baseFlameY)
                flamePath.cubicTo(
                    baseFlameX - flameWidth * 0.95f, baseFlameY - flameHeight * 0.35f,
                    baseFlameX - flameWidth * 0.65f, baseFlameY - flameHeight * 0.75f,
                    tipFlameX, tipFlameY
                )
                flamePath.cubicTo(
                    baseFlameX + flameWidth * 0.65f, baseFlameY - flameHeight * 0.75f,
                    baseFlameX + flameWidth * 0.95f, baseFlameY - flameHeight * 0.35f,
                    baseFlameX, baseFlameY
                )
                flamePath.close()

                drawPath(
                    path = flamePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF3D00).copy(alpha = 0.95f),
                            Color(0xFFFF6D00),
                            Color(0xFFFFAB00)
                        ),
                        startY = tipFlameY,
                        endY = baseFlameY
                    )
                )

                // Inner Bright Yellow Heart
                val heartWidth = flameWidth * 0.62f
                val heartHeight = flameHeight * 0.72f
                val heartTipX = baseFlameX + swayX * 0.7f
                val heartTipY = baseFlameY - heartHeight

                flamePath.reset()
                flamePath.moveTo(baseFlameX, baseFlameY - 2f)
                flamePath.cubicTo(
                    baseFlameX - heartWidth * 0.9f, baseFlameY - heartHeight * 0.35f,
                    baseFlameX - heartWidth * 0.5f, baseFlameY - heartHeight * 0.75f,
                    heartTipX, heartTipY
                )
                flamePath.cubicTo(
                    baseFlameX + heartWidth * 0.5f, baseFlameY - heartHeight * 0.75f,
                    baseFlameX + heartWidth * 0.9f, baseFlameY - heartHeight * 0.35f,
                    baseFlameX, baseFlameY - 2f
                )
                flamePath.close()

                drawPath(
                    path = flamePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD600),
                            Color(0xFFFFEA00),
                            Color(0xFFFFF59D)
                        ),
                        startY = heartTipY,
                        endY = baseFlameY
                    )
                )

                // White-Yellow Core
                val coreWidth = flameWidth * 0.32f
                val coreHeight = flameHeight * 0.44f
                val coreTipX = baseFlameX + swayX * 0.4f
                val coreTipY = baseFlameY - coreHeight

                flamePath.reset()
                flamePath.moveTo(baseFlameX, baseFlameY - 4f)
                flamePath.cubicTo(
                    baseFlameX - coreWidth, baseFlameY - coreHeight * 0.4f,
                    baseFlameX - coreWidth * 0.5f, baseFlameY - coreHeight * 0.8f,
                    coreTipX, coreTipY
                )
                flamePath.cubicTo(
                    baseFlameX + coreWidth * 0.5f, baseFlameY - coreHeight * 0.8f,
                    baseFlameX + coreWidth, baseFlameY - coreHeight * 0.4f,
                    baseFlameX, baseFlameY - 4f
                )
                flamePath.close()

                drawPath(
                    path = flamePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF9C4),
                            Color(0xFFFFFFFF)
                        ),
                        startY = coreTipY,
                        endY = baseFlameY
                    )
                )
            }

            // 10. SMOKE ANIMATION (Finish & Broken states)
            if (state.phase == CandlePhase.FINISHED || state.phase == CandlePhase.BROKEN) {
                val smokeElapsedSec = (phaseElapsedMs / 1000f).coerceIn(0f, 4f)
                if (smokeElapsedSec < 3.5f) {
                    val isFinishState = state.phase == CandlePhase.FINISHED
                    val smokeAlpha = (1f - (smokeElapsedSec / 3.0f)).coerceIn(0f, 1f)

                    if (isFinishState) {
                        // Gentle, thin curving wisp of white/cream smoke rising softly
                        val smokeHeight = 120f * (smokeElapsedSec / 2.0f).coerceAtMost(1f)
                        val wispSway1 = sin(smokeElapsedSec * 3f) * 14f
                        val wispSway2 = cos(smokeElapsedSec * 2.5f) * 22f

                        smokePath.reset()
                        smokePath.moveTo(wickTipX, wickTipY)
                        smokePath.cubicTo(
                            wickTipX + wispSway1, wickTipY - smokeHeight * 0.35f,
                            wickTipX + wispSway2, wickTipY - smokeHeight * 0.75f,
                            wickTipX + wispSway1 * 1.5f, wickTipY - smokeHeight
                        )

                        drawPath(
                            path = smokePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x00FFFFFF),
                                    Color(0xFFE6DEC8).copy(alpha = 0.4f * smokeAlpha),
                                    Color(0xFFE6DEC8).copy(alpha = 0.7f * smokeAlpha)
                                ),
                                startY = wickTipY - smokeHeight,
                                endY = wickTipY
                            ),
                            style = Stroke(
                                width = (2.5f + smokeElapsedSec * 2.5f) * density,
                                cap = StrokeCap.Round
                            )
                        )
                    } else {
                        // BROKEN: Quick, thicker, darker puff of smoke billowing up
                        val puffCount = 3
                        for (i in 0 until puffCount) {
                            val puffOffsetTime = (smokeElapsedSec - (i * 0.12f)).coerceIn(0f, 2.5f)
                            if (puffOffsetTime > 0f) {
                                val puffY = wickTipY - (puffOffsetTime * 55f)
                                val puffX = wickTipX + sin(puffOffsetTime * 4f + i) * 12f
                                val puffRadius = (8f + puffOffsetTime * 20f) * density
                                val puffAlpha = (0.65f - puffOffsetTime * 0.35f).coerceIn(0f, 0.65f)

                                drawCircle(
                                    color = Color(0xFF4A443E).copy(alpha = puffAlpha),
                                    center = Offset(puffX, puffY),
                                    radius = puffRadius
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun areAnimationsEnabled(context: Context): Boolean {
    return try {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        scale > 0f
    } catch (_: Exception) {
        true
    }
}
