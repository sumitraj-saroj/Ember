package app.ember.focus.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import app.ember.focus.domain.model.Category
import app.ember.focus.ui.common.EmberChip
import app.ember.focus.ui.common.EmberDashedChip
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.CategoryPalette
import app.ember.focus.ui.theme.CategoryPaletteHex
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.PrimaryPillFill
import app.ember.focus.ui.theme.PrimaryPillText
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SurfaceBorder
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.SurfaceCardElevated
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartSessionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFocus: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // S7: Initialize defaults from settings
    var taskName by remember { mutableStateOf("") }
    var selectedCategoryId by remember(categories, settings) {
        val initialId = settings.defaultCategoryId
        mutableStateOf(
            if (categories.any { it.id == initialId }) initialId
            else categories.firstOrNull()?.id ?: "cat_work"
        )
    }
    var goal by remember { mutableStateOf("") }
    var durationMinutes by remember(settings) {
        mutableIntStateOf(settings.defaultDurationMinutes.coerceIn(5, 180))
    }
    var isCustomDuration by remember(settings) {
        mutableStateOf(settings.defaultDurationMinutes !in listOf(25, 30, 90))
    }
    var plannedSessionCountText by remember { mutableStateOf("") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val taskNameBringIntoView = remember { BringIntoViewRequester() }
    val goalBringIntoView = remember { BringIntoViewRequester() }
    val scrollState = rememberScrollState()

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // S1: Top Bar with Close "X" + Eyebrow + Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarHeight + 8.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SecondaryPillFill)
                            .border(1.dp, SecondaryPillBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextCream,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "LIGHT A CANDLE",
                        style = EmberTheme.typography.eyebrow
                    )

                    // Spacer to balance the close button
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Begin Deep Focus",
                    style = EmberTheme.typography.taskTitle
                )
            }

            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // S2 & S3: Task Name Field (singleLine = true, 56dp high)
                Text(
                    text = "TASK NAME",
                    style = EmberTheme.typography.fieldLabel
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = {
                        Text(
                            text = "e.g. Draft launch notes",
                            style = EmberTheme.typography.body.copy(color = TextMuted.copy(alpha = 0.6f))
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCream,
                        unfocusedTextColor = TextCream,
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .bringIntoViewRequester(taskNameBringIntoView)
                        .onFocusEvent {
                            if (it.isFocused) {
                                coroutineScope.launch { taskNameBringIntoView.bringIntoView() }
                            }
                        }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // S3 & S4: Category selection with EmberChip and "+ New" dashed chip
                Text(
                    text = "CATEGORY",
                    style = EmberTheme.typography.fieldLabel
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        val isSelected = category.id == selectedCategoryId
                        val catColor = remember(category.colorHex) {
                            try {
                                Color(category.colorHex.toColorInt())
                            } catch (_: Exception) {
                                AmberAccent
                            }
                        }

                        EmberChip(
                            text = category.name,
                            selected = isSelected,
                            categoryColor = catColor,
                            onClick = { selectedCategoryId = category.id },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                            }
                        )
                    }

                    // "+ New" Dashed outline chip
                    EmberDashedChip(
                        text = "+ New",
                        onClick = { showAddCategoryDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Goal for this candle
                Text(
                    text = "GOAL FOR THIS CANDLE (OPTIONAL)",
                    style = EmberTheme.typography.fieldLabel
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    placeholder = {
                        Text(
                            text = "What specifically will you finish?",
                            style = EmberTheme.typography.body.copy(color = TextMuted.copy(alpha = 0.6f))
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCream,
                        unfocusedTextColor = TextCream,
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .bringIntoViewRequester(goalBringIntoView)
                        .onFocusEvent {
                            if (it.isFocused) {
                                coroutineScope.launch { goalBringIntoView.bringIntoView() }
                            }
                        }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // S5: Duration row (4 equal width chips: 25 min, 30 min, 90 min, Custom) + Inline Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DURATION",
                        style = EmberTheme.typography.fieldLabel
                    )
                    Text(
                        text = "$durationMinutes minutes",
                        style = EmberTheme.typography.button.copy(color = AmberAccent, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(25 to "25 min", 30 to "30 min", 90 to "90 min")
                    presets.forEach { (mins, label) ->
                        val isSelected = !isCustomDuration && durationMinutes == mins
                        EmberChip(
                            text = label,
                            selected = isSelected,
                            onClick = {
                                durationMinutes = mins
                                isCustomDuration = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Custom chip
                    EmberChip(
                        text = "Custom",
                        selected = isCustomDuration,
                        onClick = {
                            isCustomDuration = true
                            if (durationMinutes in listOf(25, 30, 90)) {
                                durationMinutes = 45
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Inline stepper when Custom duration is selected
                AnimatedVisibility(
                    visible = isCustomDuration,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Adjust duration",
                                style = EmberTheme.typography.body.copy(color = TextMuted, fontSize = 14.sp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { durationMinutes = (durationMinutes - 5).coerceAtLeast(5) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SecondaryPillFill)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease duration",
                                        tint = TextCream,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "${durationMinutes}m",
                                    style = EmberTheme.typography.button.copy(fontWeight = FontWeight.Bold),
                                    color = AmberAccent,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                IconButton(
                                    onClick = { durationMinutes = (durationMinutes + 5).coerceAtMost(180) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SecondaryPillFill)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase duration",
                                        tint = TextCream,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // S6: Planned sessions (OPTIONAL)
                Text(
                    text = "SESSIONS PLANNED (OPTIONAL)",
                    style = EmberTheme.typography.fieldLabel
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = plannedSessionCountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 2) {
                            plannedSessionCountText = input
                        }
                    },
                    placeholder = {
                        Text(
                            text = "e.g. 4",
                            style = EmberTheme.typography.body.copy(color = TextMuted.copy(alpha = 0.6f))
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCream,
                        unfocusedTextColor = TextCream,
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Shows 'session 1 of 4' on the candle screen.",
                    style = EmberTheme.typography.caption.copy(color = TextMuted, fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // S1: Fixed Bottom CTA Bar outside scroll area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .border(
                        border = BorderStroke(1.dp, SurfaceBorder.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(PrimaryPillFill)
                        .clickable {
                            val plannedCount = plannedSessionCountText.toIntOrNull()
                            viewModel.startSessionAndPersistDefaults(
                                taskName = taskName.trim(),
                                categoryId = selectedCategoryId,
                                goal = goal.trim().ifBlank { null },
                                durationMinutes = durationMinutes,
                                plannedSessionCount = plannedCount,
                                onSessionStarted = onNavigateToFocus
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = PrimaryPillText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Light the candle",
                            style = EmberTheme.typography.button,
                            color = PrimaryPillText
                        )
                    }
                }
            }
        }

        // Add Category Dialog with G4 8-Swatch Palette
        if (showAddCategoryDialog) {
            var newCategoryName by remember { mutableStateOf("") }
            // Auto-pick first unused palette color
            val usedHexes = categories.map { it.colorHex.uppercase() }.toSet()
            val autoPickHex = CategoryPaletteHex.firstOrNull { it.uppercase() !in usedHexes } ?: CategoryPaletteHex.first()
            var selectedHex by remember { mutableStateOf(autoPickHex) }

            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = {
                    Text(
                        text = "New Category",
                        style = EmberTheme.typography.taskTitle.copy(fontSize = 20.sp),
                        color = TextCream
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            placeholder = { Text("Category name", color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextCream,
                                unfocusedTextColor = TextCream,
                                focusedBorderColor = AmberAccent,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "COLOR",
                            style = EmberTheme.typography.fieldLabel
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CategoryPaletteHex.forEach { hex ->
                                val color = Color(hex.toColorInt())
                                val isSelected = hex.equals(selectedHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(2.5.dp, TextCream, CircleShape)
                                            } else Modifier
                                        )
                                        .clickable { selectedHex = hex }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.addCategory(newCategoryName.trim(), selectedHex)
                                showAddCategoryDialog = false
                            }
                        }
                    ) {
                        Text("Add", color = AmberAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCategoryDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = SurfaceCardElevated,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
