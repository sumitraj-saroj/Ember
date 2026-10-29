package app.ember.focus.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ember.focus.core.timer.ActiveSessionState
import app.ember.focus.core.timer.TimerEngine
import app.ember.focus.core.timer.TimerEvent
import app.ember.focus.core.timer.TimerStatus
import app.ember.focus.domain.model.UserSettings
import app.ember.focus.domain.repository.SessionRepository
import app.ember.focus.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val timerEngine: TimerEngine,
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val timerState: StateFlow<ActiveSessionState> = timerEngine.state
    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    private val _showReflection = MutableStateFlow(false)
    val showReflection: StateFlow<Boolean> = _showReflection.asStateFlow()

    private val _showBreak = MutableStateFlow(false)
    val showBreak: StateFlow<Boolean> = _showBreak.asStateFlow()

    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    init {
        viewModelScope.launch {
            timerEngine.events.collect { event ->
                when (event) {
                    is TimerEvent.SessionCompleted -> {
                        _showReflection.value = true
                    }
                    is TimerEvent.CandleWentOut -> {
                        _showReflection.value = true
                    }
                    else -> {}
                }
            }
        }
    }

    fun onPauseResumeClicked() {
        val current = timerState.value
        if (current.status == TimerStatus.RUNNING || current.status == TimerStatus.OVERTIME) {
            timerEngine.pauseSession()
        } else if (current.status == TimerStatus.PAUSED) {
            timerEngine.resumeSession()
        }
    }

    fun onResetClicked() {
        _showResetDialog.value = true
    }

    fun confirmReset(): Boolean {
        _showResetDialog.value = false
        return timerEngine.resetSession()
    }

    fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    fun onFinishClicked() {
        timerEngine.finishSession()
    }

    fun toggleAmbientSound() {
        val current = settings.value
        viewModelScope.launch {
            settingsRepository.updateAmbientSound(!current.ambientSoundEnabled, current.ambientSoundVolume)
        }
    }

    fun saveReflection(rating: Int?, note: String?, goalAchieved: Boolean?) {
        val currentSessionId = timerState.value.sessionId
        viewModelScope.launch {
            val session = sessionRepository.getSessionById(currentSessionId)
            if (session != null) {
                sessionRepository.updateSession(
                    session.copy(
                        rating = rating,
                        note = note,
                        goalAchieved = goalAchieved
                    )
                )
            }
            _showReflection.value = false
            if (timerState.value.status == TimerStatus.COMPLETED) {
                _showBreak.value = true
            }
        }
    }

    fun dismissReflection() {
        _showReflection.value = false
        if (timerState.value.status == TimerStatus.COMPLETED) {
            _showBreak.value = true
        }
    }

    fun dismissBreak() {
        _showBreak.value = false
    }

    fun getEstimatedEndTime(): String {
        val remainingSec = timerState.value.remainingSec
        val end = LocalTime.now().plusSeconds(remainingSec.toLong())
        return end.format(timeFormatter).lowercase()
    }
}
