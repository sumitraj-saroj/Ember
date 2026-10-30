package app.ember.focus.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ember.focus.domain.model.Category
import app.ember.focus.domain.model.UserSettings
import app.ember.focus.domain.repository.CategoryRepository
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.service.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateDailyGoal(hours: Int) {
        viewModelScope.launch {
            settingsRepository.updateDailyGoalMinutes(hours * 60)
        }
    }

    fun updateDefaultDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateDefaultDurationMinutes(minutes)
        }
    }

    fun toggleStrictMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateStrictMode(enabled)
        }
    }

    fun toggleKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateKeepScreenOn(enabled)
        }
    }

    fun toggleAmbientSound(enabled: Boolean) {
        viewModelScope.launch {
            val vol = settings.value.ambientSoundVolume
            settingsRepository.updateAmbientSound(enabled, vol)
        }
    }

    fun updateAmbientVolume(volume: Float) {
        viewModelScope.launch {
            val enabled = settings.value.ambientSoundEnabled
            settingsRepository.updateAmbientSound(enabled, volume)
        }
    }

    fun updateBreakLengths(shortBreak: Int, longBreak: Int) {
        viewModelScope.launch {
            settingsRepository.updateBreakLengths(shortBreak, longBreak)
        }
    }

    fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.updateDailyReminder(enabled, hour, minute)
            ReminderScheduler.scheduleDailyReminder(context, enabled, hour, minute)
        }
    }

    fun updateReminderDays(days: String) {
        viewModelScope.launch {
            settingsRepository.updateReminderDays(days)
        }
    }

    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            val newCat = Category(
                id = UUID.randomUUID().toString(),
                name = name,
                colorHex = colorHex,
                sortOrder = categories.value.size
            )
            categoryRepository.saveCategory(newCat)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(categoryId)
        }
    }

    fun updateFontPreset(preset: String) {
        viewModelScope.launch {
            settingsRepository.updateFontPreset(preset)
        }
    }
}
