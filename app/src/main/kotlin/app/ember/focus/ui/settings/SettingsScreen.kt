package app.ember.focus.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import app.ember.focus.BuildConfig
import app.ember.focus.domain.model.Category
import app.ember.focus.ui.common.BottomFadeScrim
import app.ember.focus.ui.common.EmberChip
import app.ember.focus.ui.common.EmberSlider
import app.ember.focus.ui.common.EmberSwitch
import app.ember.focus.ui.common.TopFadeScrim
import app.ember.focus.ui.theme.AmberAccent
import app.ember.focus.ui.theme.AshGray
import app.ember.focus.ui.theme.BackgroundBottom
import app.ember.focus.ui.theme.BackgroundTop
import app.ember.focus.ui.theme.CategoryPalette
import app.ember.focus.ui.theme.EmberTheme
import app.ember.focus.ui.theme.PrimaryPillFill
import app.ember.focus.ui.theme.PrimaryPillText
import app.ember.focus.ui.theme.SecondaryPillBorder
import app.ember.focus.ui.theme.SecondaryPillFill
import app.ember.focus.ui.theme.SecondaryPillText
import app.ember.focus.ui.theme.SurfaceCard
import app.ember.focus.ui.theme.TextCream
import app.ember.focus.ui.theme.TextMuted
import java.util.Locale

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var isCustomDurationSelected by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updateDailyReminder(true, settings.dailyReminderHour, settings.dailyReminderMinute)
        }
    }

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
                .verticalScroll(rememberScrollState())
                .padding(
                    top = topInset + 16.dp,
                    bottom = bottomInset + 24.dp
                )
        ) {
            // TOP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
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

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "EMBER",
                        style = EmberTheme.typography.eyebrow,
                        color = AmberAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Preferences",
                        style = EmberTheme.typography.taskTitle,
                        color = TextCream
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. FOCUS AMBITION (SE3, G7)
            SettingsSection(title = "FOCUS AMBITION") {
                val goalHours = settings.dailyGoalMinutes / 60
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Daily Deep Work Goal", style = EmberTheme.typography.listCardTitle, color = TextCream)
                    Text(text = "${goalHours}h", style = EmberTheme.typography.button, color = AmberAccent)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Target dedicated focus time per day.",
                    style = EmberTheme.typography.caption,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                EmberSlider(
                    value = goalHours.toFloat(),
                    onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                    valueRange = 1f..8f,
                    steps = 6,
                    showHourTicks = true,
                    contentDescription = "Daily goal hours"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Default Session Duration Chips (SE3: 25m, 30m, 90m, Custom)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Default Candle Duration", style = EmberTheme.typography.listCardTitle, color = TextCream)
                    Text(text = "${settings.defaultDurationMinutes}m", style = EmberTheme.typography.button, color = AmberAccent)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Length of each freshly lit candle.",
                    style = EmberTheme.typography.caption,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                val currentDuration = settings.defaultDurationMinutes
                val presets = listOf(25, 30, 90)
                val isStandardPreset = presets.contains(currentDuration) && !isCustomDurationSelected

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        EmberChip(
                            text = "${preset}m",
                            selected = isStandardPreset && currentDuration == preset,
                            onClick = {
                                isCustomDurationSelected = false
                                viewModel.updateDefaultDuration(preset)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    EmberChip(
                        text = "Custom",
                        selected = !isStandardPreset || isCustomDurationSelected,
                        onClick = {
                            isCustomDurationSelected = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isStandardPreset || isCustomDurationSelected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SecondaryPillFill)
                            .border(1.dp, SecondaryPillBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val next = (currentDuration - 5).coerceAtLeast(5)
                                viewModel.updateDefaultDuration(next)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextCream, modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = "$currentDuration minutes",
                            style = EmberTheme.typography.body,
                            color = AmberAccent
                        )

                        IconButton(
                            onClick = {
                                val next = (currentDuration + 5).coerceAtMost(180)
                                viewModel.updateDefaultDuration(next)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextCream, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. DISCIPLINE & DISPLAY (G5)
            SettingsSection(title = "DISCIPLINE & DISPLAY") {
                // Strict Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(text = "Strict Mode", style = EmberTheme.typography.listCardTitle, color = TextCream)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Leaving Ember for another app blows out the candle. Locking screen never breaks a session.",
                            style = EmberTheme.typography.caption,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                    EmberSwitch(
                        checked = settings.strictModeEnabled,
                        onCheckedChange = { viewModel.toggleStrictMode(it) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Keep Screen On
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(text = "Keep Screen On", style = EmberTheme.typography.listCardTitle, color = TextCream)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prevents device sleep while candle is actively burning.",
                            style = EmberTheme.typography.caption,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                    EmberSwitch(
                        checked = settings.keepScreenOn,
                        onCheckedChange = { viewModel.toggleKeepScreenOn(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. SOUND & ATMOSPHERE (G7, G5)
            SettingsSection(title = "SOUND & ATMOSPHERE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(text = "Fire Crackle Ambient Sound", style = EmberTheme.typography.listCardTitle, color = TextCream)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Plays gentle fireplace audio while candle is burning. Respects silent mode.",
                            style = EmberTheme.typography.caption,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                    EmberSwitch(
                        checked = settings.ambientSoundEnabled,
                        onCheckedChange = { viewModel.toggleAmbientSound(it) }
                    )
                }

                if (settings.ambientSoundEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sound Volume", style = EmberTheme.typography.caption, color = TextMuted)
                        Text(text = "${(settings.ambientSoundVolume * 100).toInt()}%", style = EmberTheme.typography.caption, color = AmberAccent)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    EmberSlider(
                        value = settings.ambientSoundVolume,
                        onValueChange = { viewModel.updateAmbientVolume(it) },
                        valueRange = 0.05f..1.0f,
                        contentDescription = "Sound volume"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. REST & REMINDERS (SE4)
            SettingsSection(title = "REST & REMINDERS") {
                // Take Five Break Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Take Five Break", style = EmberTheme.typography.listCardTitle, color = TextCream)
                    Text(text = "${settings.shortBreakMinutes}m", style = EmberTheme.typography.button, color = AmberAccent)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Default rest after completed session",
                    style = EmberTheme.typography.caption,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15).forEach { m ->
                        EmberChip(
                            text = "${m}m",
                            selected = settings.shortBreakMinutes == m,
                            onClick = { viewModel.updateBreakLengths(m, settings.longBreakMinutes) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Long Break Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Long Break", style = EmberTheme.typography.listCardTitle, color = TextCream)
                    Text(text = "${settings.longBreakMinutes}m", style = EmberTheme.typography.button, color = AmberAccent)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Extended rest after every 4th session",
                    style = EmberTheme.typography.caption,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 20, 30).forEach { m ->
                        EmberChip(
                            text = "${m}m",
                            selected = settings.longBreakMinutes == m,
                            onClick = { viewModel.updateBreakLengths(settings.shortBreakMinutes, m) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Daily Reminder Switch + Time & Weekday Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(text = "Daily Reminder", style = EmberTheme.typography.listCardTitle, color = TextCream)
                        Spacer(modifier = Modifier.height(4.dp))
                        val formattedTime = String.format(Locale.US, "%02d:%02d", settings.dailyReminderHour, settings.dailyReminderMinute)
                        Text(
                            text = "Sends 'Light your first candle' at $formattedTime if no work started yet.",
                            style = EmberTheme.typography.caption,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                    EmberSwitch(
                        checked = settings.dailyReminderEnabled,
                        onCheckedChange = { enable ->
                            if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.updateDailyReminder(enable, settings.dailyReminderHour, settings.dailyReminderMinute)
                            }
                        }
                    )
                }

                AnimatedVisibility(visible = settings.dailyReminderEnabled) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        // Time Picker Button
                        val timeStr = String.format(Locale.US, "%02d:%02d", settings.dailyReminderHour, settings.dailyReminderMinute)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SecondaryPillFill)
                                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    TimePickerDialog(
                                        context,
                                        { _, h, m -> viewModel.updateDailyReminder(true, h, m) },
                                        settings.dailyReminderHour,
                                        settings.dailyReminderMinute,
                                        false
                                    ).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Reminder Time", style = EmberTheme.typography.body, color = TextCream)
                            Text(text = timeStr, style = EmberTheme.typography.button, color = AmberAccent)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Weekday Chips (M T W T F S S, 40dp, selectable)
                        Text(text = "REMINDER DAYS", style = EmberTheme.typography.fieldLabel, color = TextMuted)
                        Spacer(modifier = Modifier.height(8.dp))

                        val activeDays = remember(settings.reminderDays) {
                            settings.reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                        }
                        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            (1..7).forEach { dayNum ->
                                val isSelected = activeDays.contains(dayNum)
                                val label = dayLabels[dayNum - 1]

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) AmberAccent else SecondaryPillFill)
                                        .border(1.dp, if (isSelected) AmberAccent else SecondaryPillBorder, CircleShape)
                                        .clickable {
                                            val newDays = if (isSelected) {
                                                if (activeDays.size > 1) activeDays - dayNum else activeDays
                                            } else {
                                                activeDays + dayNum
                                            }
                                            viewModel.updateReminderDays(newDays.sorted().joinToString(","))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = EmberTheme.typography.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) PrimaryPillText else TextCream
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. CATEGORIES (SE5, G4)
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
                                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(14.dp))
                                .clickable { editingCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = cat.name, style = EmberTheme.typography.body, color = TextCream)
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${cat.name}",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
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
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Add Category", style = EmberTheme.typography.button, color = AmberAccent)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. DEBUG: FONT PRESET (SE6, G8)
            if (BuildConfig.DEBUG) {
                SettingsSection(title = "TYPOGRAPHY PRESET (DEBUG)") {
                    Text(
                        text = "Font preset",
                        style = EmberTheme.typography.listCardTitle,
                        color = TextCream
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compare default variable sans (Manrope) vs variable serif (Fraunces).",
                        style = EmberTheme.typography.caption,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Manrope" to "MANROPE", "Fraunces" to "FRAUNCES").forEach { (label, value) ->
                            val isSelected = settings.fontPreset.equals(value, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) AmberAccent else SecondaryPillFill)
                                    .border(1.dp, if (isSelected) AmberAccent else SecondaryPillBorder, RoundedCornerShape(14.dp))
                                    .clickable { viewModel.updateFontPreset(value) },
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Default: Manrope",
                        style = EmberTheme.typography.caption,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 7. ABOUT EMBER (SE7)
            SettingsSection(title = "ABOUT") {
                Text(text = "Ember ${BuildConfig.VERSION_NAME}", style = EmberTheme.typography.taskTitle, color = TextCream)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "100% Offline · Zero Telemetry · Zero Internet Permission\n" +
                            "Every session is stored privately on your phone. Designed for deep craft, calm intention, and ruthless focus.",
                    style = EmberTheme.typography.caption,
                    color = TextMuted,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

        TopFadeScrim()
        BottomFadeScrim()
    }

    // Edit Category Dialog / Sheet (SE5)
    editingCategory?.let { cat ->
        var editName by remember { mutableStateOf(cat.name) }
        var editColorHex by remember { mutableStateOf(cat.colorHex) }

        AlertDialog(
            onDismissRequest = { editingCategory = null },
            containerColor = SurfaceCard,
            title = { Text("Edit Category", color = TextCream, style = EmberTheme.typography.listCardTitle) },
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
                    Text("PALETTE", style = EmberTheme.typography.fieldLabel, color = TextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CategoryPalette.forEach { c ->
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
                Row {
                    if (categories.size > 1) {
                        TextButton(
                            onClick = {
                                viewModel.deleteCategory(cat.id)
                                editingCategory = null
                            }
                        ) {
                            Text("Archive", color = AshGray)
                        }
                    }
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
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Add Category Dialog (8 swatches from CategoryPalette)
    if (showAddCategoryDialog) {
        var newName by remember { mutableStateOf("") }
        val usedColors = categories.map { it.colorHex.uppercase() }.toSet()
        val firstUnused = CategoryPalette.firstOrNull { c ->
            val hex = String.format("#%06X", (0xFFFFFF and c.hashCode()))
            !usedColors.contains(hex.uppercase())
        } ?: CategoryPalette.first()

        val initialHex = String.format("#%06X", (0xFFFFFF and firstUnused.hashCode()))
        var newColorHex by remember { mutableStateOf(initialHex) }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = SurfaceCard,
            title = { Text("New Category", color = TextCream, style = EmberTheme.typography.listCardTitle) },
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
                    Text("PALETTE", style = EmberTheme.typography.fieldLabel, color = TextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CategoryPalette.forEach { c ->
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
            style = EmberTheme.typography.eyebrow,
            color = AmberAccent,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(1.dp, SecondaryPillBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                content()
            }
        }
    }
}
