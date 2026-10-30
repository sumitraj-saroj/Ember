package app.ember.focus.ui.today

import androidx.core.graphics.toColorInt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.Task
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.CategoryColors
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.PrimaryPillFill
import app.ember.focus.ui.theme.PrimaryPillText
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SecondaryPillText
import app.ember.focus.ui.theme.SurfaceBorder
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.SurfaceCardElevated
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun StartSessionSheet(
    recentTasks: List<Task>,
    categories: List<Category>,
    defaultDurationMinutes: Int,
    onDismiss: () -> Unit,
    onAddCategory: (name: String, colorHex: String) -> Unit,
    onStartSession: (
        taskName: String,
        categoryId: String,
        goal: String?,
        durationMinutes: Int,
        plannedSessionCount: Int?
    ) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var taskName by remember { mutableStateOf("") }
    var selectedCategoryId by remember(categories) {
        mutableStateOf(categories.firstOrNull()?.id ?: "cat_work")
    }
    var goal by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableFloatStateOf(defaultDurationMinutes.toFloat()) }
    var isCustomDuration by remember {
        mutableStateOf(defaultDurationMinutes !in listOf(25, 30, 90))
    }
    var plannedSessionsText by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val taskBringIntoViewRequester = remember { BringIntoViewRequester() }
    val goalBringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // SCROLLABLE CONTENT AREA (weight 1f, verticalScroll)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 4.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "LIGHT A CANDLE",
                    style = EmberTheme.typography.topLabel,
                    color = AmberAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Begin Deep Focus",
                    style = EmberTheme.typography.taskTitle.copy(fontSize = 22.sp, lineHeight = 28.sp),
                    color = TextCream
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Task Name Field
                Text(
                    text = "TASK NAME",
                    style = EmberTheme.typography.caption,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = {
                        Text(
                            text = "e.g. Write chapter draft, Design auth flow",
                            style = EmberTheme.typography.body,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(taskBringIntoViewRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                scope.launch { taskBringIntoViewRequester.bringIntoView() }
                            }
                        },
                    textStyle = EmberTheme.typography.body,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCream,
                        unfocusedTextColor = TextCream,
                        focusedContainerColor = SurfaceCardElevated,
                        unfocusedContainerColor = SurfaceCardElevated,
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = SecondaryPillBorder
                    )
                )

                // Autocomplete suggestions from recent tasks
                if (taskName.isNotBlank() && recentTasks.any { it.name.contains(taskName, ignoreCase = true) && !it.name.equals(taskName, ignoreCase = true) }) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentTasks.filter { it.name.contains(taskName, ignoreCase = true) && !it.name.equals(taskName, ignoreCase = true) }
                            .take(4)
                            .forEach { suggestion ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SecondaryPillFill)
                                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            taskName = suggestion.name
                                            selectedCategoryId = suggestion.categoryId
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = suggestion.name,
                                        style = EmberTheme.typography.caption,
                                        color = TextCream
                                    )
                                }
                            }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORY",
                        style = EmberTheme.typography.caption,
                        color = TextMuted
                    )
                    Text(
                        text = "+ New Category",
                        style = EmberTheme.typography.caption,
                        color = AmberAccent,
                        modifier = Modifier.clickable { showAddCategoryDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        val catColor = try {
                            Color(cat.colorHex.toColorInt())
                        } catch (_: Exception) {
                            AmberAccent
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) catColor.copy(alpha = 0.22f) else SecondaryPillFill)
                                .border(1.dp, if (isSelected) catColor else SecondaryPillBorder, RoundedCornerShape(14.dp))
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                style = EmberTheme.typography.caption,
                                color = if (isSelected) TextCream else TextMuted,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Goal field (optional one-liner)
                Text(
                    text = "GOAL FOR THIS CANDLE (OPTIONAL)",
                    style = EmberTheme.typography.caption,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    placeholder = {
                        Text(
                            text = "What specifically will you finish?",
                            style = EmberTheme.typography.body,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(goalBringIntoViewRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                scope.launch { goalBringIntoViewRequester.bringIntoView() }
                            }
                        },
                    textStyle = EmberTheme.typography.body,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCream,
                        unfocusedTextColor = TextCream,
                        focusedContainerColor = SurfaceCardElevated,
                        unfocusedContainerColor = SurfaceCardElevated,
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = SecondaryPillBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Duration section: compact single row of chips (25, 30, 90, custom)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DURATION",
                        style = EmberTheme.typography.caption,
                        color = TextMuted
                    )
                    Text(
                        text = "${durationMinutes.toInt()} minutes",
                        style = EmberTheme.typography.listCardTitle,
                        color = TextCream
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Compact single row of duration chips: 25, 30, 90, and Custom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25, 30, 90).forEach { preset ->
                        val isSelected = !isCustomDuration && durationMinutes.toInt() == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) AmberAccent else SecondaryPillFill)
                                .border(1.dp, if (isSelected) AmberAccent else SecondaryPillBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    isCustomDuration = false
                                    durationMinutes = preset.toFloat()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$preset min",
                                style = EmberTheme.typography.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) PrimaryPillText else SecondaryPillText
                            )
                        }
                    }

                    // Custom chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCustomDuration) AmberAccent else SecondaryPillFill)
                            .border(1.dp, if (isCustomDuration) AmberAccent else SecondaryPillBorder, RoundedCornerShape(14.dp))
                            .clickable { isCustomDuration = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCustomDuration) "${durationMinutes.toInt()}m" else "Custom",
                            style = EmberTheme.typography.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCustomDuration) PrimaryPillText else SecondaryPillText
                        )
                    }
                }

                // If Custom selected, show slider
                AnimatedVisibility(
                    visible = isCustomDuration,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Slider(
                            value = durationMinutes,
                            onValueChange = { durationMinutes = it },
                            valueRange = 5f..180f,
                            steps = 34,
                            colors = SliderDefaults.colors(
                                thumbColor = AmberAccent,
                                activeTrackColor = AmberAccent,
                                inactiveTrackColor = SecondaryPillFill
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Planned sessions count (optional)
                Text(
                    text = "PLAN N SESSIONS FOR THIS TASK (OPTIONAL)",
                    style = EmberTheme.typography.caption,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = plannedSessionsText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) plannedSessionsText = it },
                    placeholder = {
                        Text(
                            text = "e.g. 4 (shows 'session X of 4')",
                            style = EmberTheme.typography.body,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = EmberTheme.typography.body,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextCream,
                        unfocusedTextColor = TextCream,
                        focusedContainerColor = SurfaceCardElevated,
                        unfocusedContainerColor = SurfaceCardElevated,
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = SecondaryPillBorder
                    )
                )
            }

            // Subtle top divider where content scrolls under it
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceBorder)
            )

            // FIXED BOTTOM BAR (outside scroll area, with navigationBarsPadding and imePadding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
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
                            val plannedCount = plannedSessionsText.toIntOrNull()
                            val finalTask = taskName.trim().ifBlank { "Deep work" }
                            val finalGoal = goal.trim().ifBlank { null }
                            onStartSession(
                                finalTask,
                                selectedCategoryId,
                                finalGoal,
                                durationMinutes.toInt(),
                                plannedCount
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
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
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf("#E8A15A") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = SurfaceCard,
            title = {
                Text(
                    text = "New Category",
                    color = TextCream,
                    style = EmberTheme.typography.listCardTitle
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        placeholder = {
                            Text(
                                text = "Category name",
                                style = EmberTheme.typography.body,
                                color = TextMuted
                            )
                        },
                        textStyle = EmberTheme.typography.body,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextCream,
                            unfocusedTextColor = TextCream,
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = SecondaryPillBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Palette",
                        style = EmberTheme.typography.caption,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryColors.forEach { c ->
                            val hex = String.format("#%06X", (0xFFFFFF and c.hashCode()))
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            onAddCategory(newCatName.trim(), selectedColorHex)
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "Add",
                        style = EmberTheme.typography.button,
                        color = AmberAccent
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text(
                        text = "Cancel",
                        style = EmberTheme.typography.button,
                        color = TextMuted
                    )
                }
            }
        )
    }
}
