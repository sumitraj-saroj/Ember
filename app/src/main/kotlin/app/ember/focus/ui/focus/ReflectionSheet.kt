package app.ember.focus.ui.focus

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionSheet(
    goal: String?,
    onDismiss: () -> Unit,
    onSaveReflection: (rating: Int?, note: String?, goalAchieved: Boolean?) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var rating by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }
    var goalAchieved by remember { mutableStateOf<Boolean?>(null) }

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
        ) {
            Text(
                text = "SESSION REFLECTION",
                style = Typography.labelSmall,
                color = AmberAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "How was your focus?",
                style = Typography.headlineSmall,
                color = TextCream
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1 to 5 Ember rating icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val isLit = i <= rating
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isLit) AmberAccent.copy(alpha = 0.18f) else SecondaryPillFill)
                            .border(
                                1.dp,
                                if (isLit) AmberAccent else SecondaryPillBorder,
                                CircleShape
                            )
                            .clickable { rating = if (rating == i) 0 else i },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Rating $i ember",
                            tint = if (isLit) AmberAccent else AshGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Goal hit question (if goal was set)
            if (!goal.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Did you hit your goal?",
                    style = Typography.titleMedium,
                    color = TextCream
                )
                Text(
                    text = "\"$goal\"",
                    style = Typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GoalPill(
                        text = "Yes, completed",
                        selected = goalAchieved == true,
                        onClick = { goalAchieved = true }
                    )
                    GoalPill(
                        text = "Not quite",
                        selected = goalAchieved == false,
                        onClick = { goalAchieved = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Short note input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Add a short note or insight (optional)", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextCream,
                    unfocusedTextColor = TextCream,
                    focusedContainerColor = SurfaceCardElevated,
                    unfocusedContainerColor = SurfaceCardElevated,
                    focusedBorderColor = AmberAccent,
                    unfocusedBorderColor = SecondaryPillBorder
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons: Skip and Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SecondaryPillFill)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(24.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Skip",
                        style = Typography.labelLarge,
                        color = SecondaryPillText
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimaryPillFill)
                        .clickable {
                            val finalRating = if (rating > 0) rating else null
                            val finalNote = note.trim().ifEmpty { null }
                            onSaveReflection(finalRating, finalNote, goalAchieved)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save reflection",
                        style = Typography.labelLarge,
                        color = PrimaryPillText
                    )
                }
            }
        }
    }
}

@Composable
fun GoalPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) AmberAccent else SecondaryPillFill)
            .border(1.dp, if (selected) AmberAccent else SecondaryPillBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (selected) PrimaryPillText else SecondaryPillText
        )
    }
}
