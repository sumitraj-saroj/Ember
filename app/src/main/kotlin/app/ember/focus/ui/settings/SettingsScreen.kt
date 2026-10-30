package app.ember.focus.ui.settings

import androidx.core.graphics.toColorInt
import java.util.Locale

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import app.ember.focus.BuildConfig
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.ember.focus.domain.model.Category
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.CategoryColors
import app.ember.focus.ui.theme.EmberTheme
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
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updateDailyReminder(true, settings.dailyReminderHour, settings.dailyReminderMinute)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom)
                )
            )
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SecondaryPillFill)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextCream
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "EMBER",
                    style = Typography.labelSmall,
                    color = AmberAccent
                )
                Text(
                    text = "Preferences",
                    style = Typography.titleLarge,
                    color = TextCream
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. FOCUS AMBITION (Daily Goal & Default Duration)
        SettingsSection(title = "FOCUS AMBITION") {
            // Daily Goal
            val goalHours = settings.dailyGoalMinutes / 60
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Daily Deep Work Goal", style = Typography.bodyLarge, color = TextCream)
                Text(text = "${goalHours}h", style = Typography.titleMedium, color = AmberAccent)
            }
            Slider(
                value = goalHours.toFloat(),
                onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                valueRange = 1f..8f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = AmberAccent,
                    activeTrackColor = AmberAccent,
                    inactiveTrackColor = SecondaryPillFill
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Default Session Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Default Candle Duration", style = Typography.bodyLarge, color = TextCream)
                Text(text = "${settings.defaultDurationMinutes}m", style = Typography.titleMedium, color = AmberAccent)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(25, 30, 45, 90).forEach { preset ->
                    val isSelected = settings.defaultDurationMinutes == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AmberAccent else SecondaryPillFill)
                            .clickable { viewModel.updateDefaultDuration(preset) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset}m",
                            style = EmberTheme.typography.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) PrimaryPillText else SecondaryPillText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. DISCIPLINE & DISPLAY
        SettingsSection(title = "DISCIPLINE & DISPLAY") {
            // Strict Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(text = "Strict Mode", style = Typography.bodyLarge, color = TextCream)
                    Text(
                        text = "Leaving Ember for another app blows out the candle. Locking screen never breaks a session.",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = settings.strictModeEnabled,
                    onCheckedChange = { viewModel.toggleStrictMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryPillText,
                        checkedTrackColor = AmberAccent,
                        uncheckedTrackColor = SecondaryPillFill
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keep Screen On
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(text = "Keep Screen On", style = Typography.bodyLarge, color = TextCream)
                    Text(
                        text = "Prevents device sleep while candle is actively burning.",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = settings.keepScreenOn,
                    onCheckedChange = { viewModel.toggleKeepScreenOn(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryPillText,
                        checkedTrackColor = AmberAccent,
                        uncheckedTrackColor = SecondaryPillFill
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. SOUND & ATMOSPHERE
        SettingsSection(title = "SOUND & ATMOSPHERE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(text = "Fire Crackle Ambient Sound", style = Typography.bodyLarge, color = TextCream)
                    Text(
                        text = "Plays gentle fireplace loop only while candle is burning. Respects silent mode.",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = settings.ambientSoundEnabled,
                    onCheckedChange = { viewModel.toggleAmbientSound(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryPillText,
                        checkedTrackColor = AmberAccent,
                        uncheckedTrackColor = SecondaryPillFill
                    )
                )
            }

            if (settings.ambientSoundEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sound Volume", style = Typography.bodySmall, color = TextMuted)
                    Text(text = "${(settings.ambientSoundVolume * 100).toInt()}%", style = Typography.bodySmall, color = AmberAccent)
                }
                Slider(
                    value = settings.ambientSoundVolume,
                    onValueChange = { viewModel.updateAmbientVolume(it) },
                    valueRange = 0.05f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberAccent,
                        activeTrackColor = AmberAccent,
                        inactiveTrackColor = SecondaryPillFill
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. REST & REMINDERS
        SettingsSection(title = "REST & REMINDERS") {
            // Break Lengths
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Take Five Break", style = Typography.bodyLarge, color = TextCream)
                    Text(text = "Default rest after completed session", style = Typography.bodySmall, color = TextMuted)
                }
                Text(text = "${settings.shortBreakMinutes}m", style = Typography.titleMedium, color = AmberAccent)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(text = "Daily Reminder", style = Typography.bodyLarge, color = TextCream)
                    val timeStr = String.format(Locale.US, "%02d:%02d", settings.dailyReminderHour, settings.dailyReminderMinute)
                    Text(
                        text = "Sends 'Light your first candle' at $timeStr if no work started yet.",
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = settings.dailyReminderEnabled,
                    onCheckedChange = { enable ->
                        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.updateDailyReminder(enable, settings.dailyReminderHour, settings.dailyReminderMinute)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryPillText,
                        checkedTrackColor = AmberAccent,
                        uncheckedTrackColor = SecondaryPillFill
                    )
                )
            }

            if (settings.dailyReminderEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondaryPillFill)
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m -> viewModel.updateDailyReminder(true, h, m) },
                                settings.dailyReminderHour,
                                settings.dailyReminderMinute,
                                false
                            ).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    val formatted = String.format(Locale.US, "%02d:%02d", settings.dailyReminderHour, settings.dailyReminderMinute)
                    Text(text = "Change Reminder Time: $formatted", style = EmberTheme.typography.caption, color = AmberAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. CATEGORIES
        SettingsSection(title = "CATEGORIES") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    val catColor = try {
                        Color(cat.colorHex.toColorInt())
                    } catch (_: Exception) {
                        AmberAccent
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SecondaryPillFill)
                            .clickable { editingCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = cat.name, style = Typography.bodyMedium, color = TextCream)
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit category",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SecondaryPillFill)
                        .border(1.dp, SecondaryPillBorder, RoundedCornerShape(14.dp))
                        .clickable { showAddCategoryDialog = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Add Category", style = Typography.labelMedium, color = AmberAccent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DEBUG: FONT PRESET
        if (BuildConfig.DEBUG) {
            SettingsSection(title = "TYPOGRAPHY PRESET (DEBUG)") {
                Text(
                    text = "Font preset",
                    style = EmberTheme.typography.listCardTitle,
                    color = TextCream
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Compare default modern variable sans (Manrope) vs variable serif (Fraunces).",
                    style = EmberTheme.typography.caption,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Manrope (Default)" to "MANROPE", "Fraunces (Serif)" to "FRAUNCES").forEach { (label, value) ->
                        val isSelected = settings.fontPreset.equals(value, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) AmberAccent else SecondaryPillFill)
                                .border(1.dp, if (isSelected) AmberAccent else SecondaryPillBorder, RoundedCornerShape(14.dp))
                                .clickable { viewModel.updateFontPreset(value) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = EmberTheme.typography.button,
                                color = if (isSelected) PrimaryPillText else SecondaryPillText
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 6. ABOUT EMBER
        SettingsSection(title = "ABOUT") {
            Text(text = "Ember 1.0", style = Typography.titleMedium, color = TextCream)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "100% Offline · Zero Telemetry · Zero Internet Permission\n" +
                        "Every session is stored privately on your phone. Designed for deep craft, calm intention, and ruthless focus.",
                style = Typography.bodySmall,
                color = TextMuted,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    // Edit Category Dialog
    editingCategory?.let { cat ->
        var editName by remember { mutableStateOf(cat.name) }
        var editColorHex by remember { mutableStateOf(cat.colorHex) }

        AlertDialog(
            onDismissRequest = { editingCategory = null },
            containerColor = SurfaceCard,
            title = { Text("Edit Category", color = TextCream, style = Typography.titleLarge) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryColors.forEach { c ->
                            val hex = String.format("#%06X", (0xFFFFFF and c.hashCode()))
                            val isSelected = editColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { editColorHex = hex },
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
                        if (editName.isNotBlank()) {
                            viewModel.updateCategory(cat.copy(name = editName.trim(), colorHex = editColorHex))
                            editingCategory = null
                        }
                    }
                ) {
                    Text("Save", color = AmberAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        var newName by remember { mutableStateOf("") }
        var newColorHex by remember { mutableStateOf("#E8A15A") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = SurfaceCard,
            title = { Text("New Category", color = TextCream, style = Typography.titleLarge) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryColors.forEach { c ->
                            val hex = String.format("#%06X", (0xFFFFFF and c.hashCode()))
                            val isSelected = newColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { newColorHex = hex },
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
                        if (newName.isNotBlank()) {
                            viewModel.addCategory(newName.trim(), newColorHex)
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

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            style = Typography.labelSmall,
            color = AmberAccent,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                content()
            }
        }
    }
}
