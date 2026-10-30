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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import app.ember.focus.ui.common.BottomFadeScrim
import app.ember.focus.ui.common.EmberChip
import app.ember.focus.ui.common.EmberSlider
import app.ember.focus.ui.common.TopFadeScrim
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CandleLabScreen(
    onNavigateBack: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0.15f) }
    var phase by remember { mutableStateOf(CandlePhase.BURNING) }
    var seed by remember { mutableStateOf("ember_demo_seed") }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topInset + 12.dp, bottom = bottomInset + 16.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
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
                        style = EmberTheme.typography.eyebrow,
                        color = AmberAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Candle Motion Tuner",
                        style = EmberTheme.typography.listCardTitle,
                        color = TextCream
                    )
                }

                // L1: Visible label next to dice button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SecondaryPillFill)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                        .clickable { seed = UUID.randomUUID().toString() }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Randomize drips",
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Randomize drips",
                        style = EmberTheme.typography.caption,
                        color = TextCream
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Candle View Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Controls Bottom Sheet Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SecondaryPillBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Progress Header & Slider (L1: EmberSlider)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BURN PROGRESS",
                        style = EmberTheme.typography.fieldLabel,
                        color = TextMuted
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = EmberTheme.typography.listCardTitle,
                        color = AmberAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                EmberSlider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..1f,
                    contentDescription = "Burn progress"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Preset Progress Buttons (EmberChip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.0f, 0.25f, 0.50f, 0.75f, 1.0f).forEach { preset ->
                        val isSelected = kotlin.math.abs(progress - preset) < 0.02f
                        EmberChip(
                            text = "${(preset * 100).toInt()}%",
                            selected = isSelected,
                            onClick = { progress = preset },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Phase Buttons
                Text(
                    text = "ANIMATION PHASE",
                    style = EmberTheme.typography.fieldLabel,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CandlePhase.entries.forEach { p ->
                        val isSelected = phase == p
                        EmberChip(
                            text = p.name,
                            selected = isSelected,
                            onClick = { phase = p }
                        )
                    }
                }
            }
        }

        TopFadeScrim()
        BottomFadeScrim()
    }
}
