package app.ember.focus.ui.today

import androidx.core.graphics.toColorInt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.Session
import app.ember.focus.domain.model.SessionStatus
import app.ember.focus.ui.focus.GoalPill
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.AshGray
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailSheet(
    session: Session,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onUpdateSession: (Session) -> Unit,
    onDeleteSession: (String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var taskName by remember(session) { mutableStateOf(session.taskName) }
    var selectedCategoryId by remember(session) { mutableStateOf(session.categoryId) }
    var goal by remember(session) { mutableStateOf(session.goal ?: "") }
    var goalAchieved by remember(session) { mutableStateOf(session.goalAchieved) }
    var rating by remember(session) { mutableIntStateOf(session.rating ?: 0) }
    var note by remember(session) { mutableStateOf(session.note ?: "") }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val zoneId = remember { ZoneId.systemDefault() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val startTime = remember(session.startedAt) {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(session.startedAt), zoneId).format(timeFormatter).lowercase()
    }
    val endTime = remember(session.endedAt) {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(session.endedAt), zoneId).format(timeFormatter).lowercase()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SESSION DETAIL",
                        style = Typography.labelSmall,
                        color = AmberAccent
                    )
                    Text(
                        text = "$startTime - $endTime",
                        style = Typography.titleMedium,
                        color = TextCream
                    )
                }

                if (session.status == SessionStatus.BROKEN) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AshGray.copy(alpha = 0.2f))
                            .border(1.dp, AshGray, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Broken · ${session.focusedSec / 60}m counted",
                            fontSize = 11.sp,
                            color = AshGray
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberAccent.copy(alpha = 0.2f))
                            .border(1.dp, AmberAccent, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${session.focusedSec / 60}m completed",
                            fontSize = 11.sp,
                            color = AmberAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Task Name
            Text(text = "TASK", style = Typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextCream,
                    unfocusedTextColor = TextCream,
                    focusedContainerColor = SurfaceCardElevated,
                    unfocusedContainerColor = SurfaceCardElevated,
                    focusedBorderColor = AmberAccent,
                    unfocusedBorderColor = SecondaryPillBorder
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips
            Text(text = "CATEGORY", style = Typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val catColor = try {
                        Color(cat.colorHex.toColorInt())
                    } catch (_: Exception) {
                        AmberAccent
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) catColor.copy(alpha = 0.25f) else SecondaryPillFill)
                            .border(1.dp, if (isSelected) catColor else SecondaryPillBorder, RoundedCornerShape(14.dp))
                            .clickable { selectedCategoryId = cat.id }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat.name,
                            fontSize = 12.sp,
                            color = if (isSelected) TextCream else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Focus Rating (1-5 embers)
            Text(text = "FOCUS QUALITY", style = Typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..5) {
                    val isLit = i <= rating
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isLit) AmberAccent.copy(alpha = 0.18f) else SecondaryPillFill)
                            .border(1.dp, if (isLit) AmberAccent else SecondaryPillBorder, CircleShape)
                            .clickable { rating = if (rating == i) 0 else i },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Rating $i",
                            tint = if (isLit) AmberAccent else AshGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal
            Text(text = "GOAL", style = Typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Session goal", color = TextMuted) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextCream,
                    unfocusedTextColor = TextCream,
                    focusedContainerColor = SurfaceCardElevated,
                    unfocusedContainerColor = SurfaceCardElevated,
                    focusedBorderColor = AmberAccent,
                    unfocusedBorderColor = SecondaryPillBorder
                )
            )

            if (goal.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GoalPill(
                        text = "Goal achieved",
                        selected = goalAchieved == true,
                        onClick = { goalAchieved = true }
                    )
                    GoalPill(
                        text = "Not achieved",
                        selected = goalAchieved == false,
                        onClick = { goalAchieved = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note
            Text(text = "NOTE", style = Typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Reflection notes", color = TextMuted) },
                shape = RoundedCornerShape(16.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextCream,
                    unfocusedTextColor = TextCream,
                    focusedContainerColor = SurfaceCardElevated,
                    unfocusedContainerColor = SurfaceCardElevated,
                    focusedBorderColor = AmberAccent,
                    unfocusedBorderColor = SecondaryPillBorder
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Delete & Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SecondaryPillFill)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                        .clickable { showDeleteConfirm = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete session",
                        tint = AshGray
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(PrimaryPillFill)
                        .clickable {
                            val cat = categories.find { it.id == selectedCategoryId }
                            val updated = session.copy(
                                taskName = taskName.trim().ifBlank { session.taskName },
                                categoryId = selectedCategoryId,
                                categoryName = cat?.name ?: session.categoryName,
                                categoryColorHex = cat?.colorHex ?: session.categoryColorHex,
                                goal = goal.trim().ifEmpty { null },
                                goalAchieved = goalAchieved,
                                rating = if (rating > 0) rating else null,
                                note = note.trim().ifEmpty { null }
                            )
                            onUpdateSession(updated)
                            onDismiss()
                        }
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save changes",
                        style = Typography.labelLarge,
                        color = PrimaryPillText
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SurfaceCard,
            title = { Text("Delete session?", color = TextCream, style = Typography.titleLarge) },
            text = { Text("This will permanently remove this session from your deep work log.", color = TextMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(session.id)
                        showDeleteConfirm = false
                        onDismiss()
                    }
                ) {
                    Text("Delete", color = Color(0xFFE57373))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}
