package app.ember.focus.ui.today

import androidx.core.graphics.toColorInt

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import app.ember.focus.ui.theme.PrimaryPillFill
import app.ember.focus.ui.theme.PrimaryPillText
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SecondaryPillText
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.SurfaceCardElevated
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import app.ember.focus.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var plannedSessionsText by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "LIGHT A CANDLE",
                style = Typography.labelSmall,
                color = AmberAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Begin Deep Focus",
                style = Typography.headlineSmall,
                color = TextCream
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Task Name Field
            Text(
                text = "TASK NAME",
                style = Typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                placeholder = { Text("e.g. Write chapter draft, Design auth flow", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(8.dp))
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
                                    fontSize = 12.sp,
                                    color = TextCream
                                )
                            }
                        }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORY",
                    style = Typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "+ New Category",
                    fontSize = 12.sp,
                    color = AmberAccent,
                    modifier = Modifier.clickable { showAddCategoryDialog = true }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

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
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) catColor.copy(alpha = 0.25f) else SecondaryPillFill)
                            .border(1.dp, if (isSelected) catColor else SecondaryPillBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedCategoryId = cat.id }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(catColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cat.name,
                            fontSize = 13.sp,
                            color = if (isSelected) TextCream else TextMuted,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Goal field (optional one-liner)
            Text(
                text = "GOAL FOR THIS CANDLE (OPTIONAL)",
                style = Typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                placeholder = { Text("What specifically will you finish?", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Duration Slider & Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DURATION",
                    style = Typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "${durationMinutes.toInt()} minutes",
                    style = Typography.titleMedium,
                    color = TextCream
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = durationMinutes,
                onValueChange = { durationMinutes = it },
                valueRange = 5f..180f,
                steps = 34, // 5 minute increments
                colors = SliderDefaults.colors(
                    thumbColor = AmberAccent,
                    activeTrackColor = AmberAccent,
                    inactiveTrackColor = SecondaryPillFill
                )
            )

            // Preset duration pills: 25, 30, 90 min
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(25, 30, 90).forEach { preset ->
                    val isSelected = durationMinutes.toInt() == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) AmberAccent else SecondaryPillFill)
                            .border(1.dp, if (isSelected) AmberAccent else SecondaryPillBorder, RoundedCornerShape(14.dp))
                            .clickable { durationMinutes = preset.toFloat() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$preset min",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) PrimaryPillText else SecondaryPillText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Planned sessions count (optional)
            Text(
                text = "PLAN N SESSIONS FOR THIS TASK (OPTIONAL)",
                style = Typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = plannedSessionsText,
                onValueChange = { if (it.all { c -> c.isDigit() }) plannedSessionsText = it },
                placeholder = { Text("e.g. 4 (shows 'session X of 4')", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(28.dp))

            // Big "Light the candle" button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
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
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = PrimaryPillText,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Light the candle",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPillText
                    )
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
            title = { Text("New Category", color = TextCream, style = Typography.titleLarge) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        placeholder = { Text("Category name", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextCream,
                            unfocusedTextColor = TextCream,
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = SecondaryPillBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Palette", style = Typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                    Text("Add", color = AmberAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}
