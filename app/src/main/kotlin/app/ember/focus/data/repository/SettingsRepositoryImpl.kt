package app.ember.focus.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import app.ember.focus.domain.model.UserSettings
import app.ember.focus.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ember_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val DEFAULT_DURATION_MINUTES = intPreferencesKey("default_duration_minutes")
        val STRICT_MODE_ENABLED = booleanPreferencesKey("strict_mode_enabled")
        val AMBIENT_SOUND_ENABLED = booleanPreferencesKey("ambient_sound_enabled")
        val AMBIENT_SOUND_VOLUME = floatPreferencesKey("ambient_sound_volume")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SHORT_BREAK_MINUTES = intPreferencesKey("short_break_minutes")
        val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FONT_PRESET = stringPreferencesKey("font_preset")
    }

    override val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            dailyGoalMinutes = prefs[PreferencesKeys.DAILY_GOAL_MINUTES] ?: 240,
            defaultDurationMinutes = prefs[PreferencesKeys.DEFAULT_DURATION_MINUTES] ?: 25,
            strictModeEnabled = prefs[PreferencesKeys.STRICT_MODE_ENABLED] ?: true,
            ambientSoundEnabled = prefs[PreferencesKeys.AMBIENT_SOUND_ENABLED] ?: true,
            ambientSoundVolume = prefs[PreferencesKeys.AMBIENT_SOUND_VOLUME] ?: 0.5f,
            keepScreenOn = prefs[PreferencesKeys.KEEP_SCREEN_ON] ?: true,
            shortBreakMinutes = prefs[PreferencesKeys.SHORT_BREAK_MINUTES] ?: 5,
            longBreakMinutes = prefs[PreferencesKeys.LONG_BREAK_MINUTES] ?: 15,
            dailyReminderEnabled = prefs[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderHour = prefs[PreferencesKeys.DAILY_REMINDER_HOUR] ?: 9,
            dailyReminderMinute = prefs[PreferencesKeys.DAILY_REMINDER_MINUTE] ?: 0,
            onboardingCompleted = prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            fontPreset = prefs[PreferencesKeys.FONT_PRESET] ?: "MANROPE"
        )
    }

    override suspend fun updateDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_GOAL_MINUTES] = minutes }
    }

    override suspend fun updateDefaultDurationMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_DURATION_MINUTES] = minutes }
    }

    override suspend fun updateStrictMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.STRICT_MODE_ENABLED] = enabled }
    }

    override suspend fun updateAmbientSound(enabled: Boolean, volume: Float) {
        context.dataStore.edit {
            it[PreferencesKeys.AMBIENT_SOUND_ENABLED] = enabled
            it[PreferencesKeys.AMBIENT_SOUND_VOLUME] = volume
        }
    }

    override suspend fun updateKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEEP_SCREEN_ON] = enabled }
    }

    override suspend fun updateBreakLengths(shortBreak: Int, longBreak: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.SHORT_BREAK_MINUTES] = shortBreak
            it[PreferencesKeys.LONG_BREAK_MINUTES] = longBreak
        }
    }

    override suspend fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
            it[PreferencesKeys.DAILY_REMINDER_HOUR] = hour
            it[PreferencesKeys.DAILY_REMINDER_MINUTE] = minute
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun updateFontPreset(preset: String) {
        context.dataStore.edit { it[PreferencesKeys.FONT_PRESET] = preset }
    }
}
