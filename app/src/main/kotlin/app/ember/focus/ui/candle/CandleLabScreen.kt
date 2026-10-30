package app.ember.focus.ui.candle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.PrimaryPillFill
import app.ember.focus.ui.theme.PrimaryPillText
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SecondaryPillText
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import app.ember.focus.ui.theme.Typography
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CandleLabScreen(
    onNavigateBack: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0.15f) }
    var phase by remember { mutableStateOf(CandlePhase.BURNING) }
    var seed by remember { mutableStateOf("ember_demo_seed") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom)
                )
            )
            .safeDrawingPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SecondaryPillFill)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextCream
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "EMBER LAB",
                    style = Typography.labelMedium,
                    color = AmberAccent
                )
                Text(
                    text = "Candle Motion Tuner",
                    style = Typography.titleMedium,
                    color = TextCream
                )
            }

            IconButton(
                onClick = { seed = UUID.randomUUID().toString() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SecondaryPillFill)
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Randomize drips",
                    tint = TextCream
                )
            }
        }

        // Live Candle View Canvas Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            CandleView(
                state = CandleState(
                    progress = progress,
                    phase = phase,
                    seed = seed
                )
            )
        }

        // Controls Bottom Sheet Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(SurfaceCard)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Progress Header & Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BURN PROGRESS",
                    style = Typography.labelSmall,
                    color = AmberAccent
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = Typography.titleMedium,
                    color = TextCream
                )
            }

            Slider(
                value = progress,
                onValueChange = { progress = it },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = AmberAccent,
                    activeTrackColor = AmberAccent,
                    inactiveTrackColor = SecondaryPillFill
                )
            )

            // Preset Progress Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.0f, 0.25f, 0.50f, 0.75f, 1.0f).forEach { preset ->
                    val isSelected = kotlin.math.abs(progress - preset) < 0.02f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryPillFill else SecondaryPillFill)
                            .clickable { progress = preset }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(preset * 100).toInt()}%",
                            style = EmberTheme.typography.caption,
                            color = if (isSelected) PrimaryPillText else SecondaryPillText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phase Buttons
            Text(
                text = "ANIMATION PHASE",
                style = Typography.labelSmall,
                color = AmberAccent
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CandlePhase.entries.forEach { p ->
                    val isSelected = phase == p
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AmberAccent else SecondaryPillFill)
                            .border(1.dp, if (isSelected) AmberAccent else SecondaryPillBorder, RoundedCornerShape(16.dp))
                            .clickable { phase = p }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = p.name,
                            style = EmberTheme.typography.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) PrimaryPillText else SecondaryPillText
                        )
                    }
                }
            }
        }
    }
}
