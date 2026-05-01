package com.revolution.ai.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.revolution.ai.data.db.AppDatabase
import com.revolution.ai.data.model.*
import com.revolution.ai.data.preferences.UserPreferences
import com.revolution.ai.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)
    private val repository = AppRepository(AppDatabase.getInstance(application))

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _lastResponse = MutableStateFlow("How can I help you?")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    val wakeWord: StateFlow<String> = prefs.wakeWord
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Nikhil")

    val aiName: StateFlow<String> = prefs.aiName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Revolution")

    val appName: StateFlow<String> = prefs.appName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Revolution")

    val isDarkTheme: StateFlow<Boolean> = prefs.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isAlwaysListening: StateFlow<Boolean> = prefs.isAlwaysListening
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val interactionMode: StateFlow<String> = prefs.interactionMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "VOICE")

    val voicePitch: StateFlow<Float> = prefs.voicePitch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val voiceSpeed: StateFlow<Float> = prefs.voiceSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val voiceId: StateFlow<String> = prefs.voiceId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")

    val logEmail: StateFlow<String> = prefs.logEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "hnamproject@gmail.com")

    val dailySummaryEnabled: StateFlow<Boolean> = prefs.dailySummaryEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isWorkMode: StateFlow<Boolean> = prefs.isWorkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val cloudSync: StateFlow<Boolean> = prefs.cloudSync
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val workWhenLocked: StateFlow<Boolean> = prefs.workWhenLocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val urgencyEnabled: StateFlow<Boolean> = prefs.urgencyEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val recentLogs: StateFlow<List<ActionLog>> = repository.getRecentLogs(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ActionLog>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appPermissions: StateFlow<List<AppPermissionEntry>> = repository.getAllAppPermissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedKeywords: StateFlow<List<BlockedKeyword>> = repository.getAllBlockedKeywords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAssistantState(state: AssistantState) {
        _assistantState.value = state
    }

    fun toggleListening() {
        _assistantState.value = when (_assistantState.value) {
            AssistantState.LISTENING -> AssistantState.IDLE
            else -> AssistantState.LISTENING
        }
    }

    fun processTextCommand(command: String) {
        if (command.isBlank()) return
        _assistantState.value = AssistantState.PROCESSING
        viewModelScope.launch {
            repository.insertLog(
                ActionLog(
                    command = command,
                    action = "text_command",
                    result = "Processed: $command",
                    wasSuccessful = true,
                    category = "general"
                )
            )
            _lastResponse.value = "Processing: \"$command\""
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun <T> updatePreference(key: androidx.datastore.preferences.core.Preferences.Key<T>, value: T) {
        viewModelScope.launch { prefs.update(key, value) }
    }

    fun deleteOldLogs(olderThanMillis: Long) {
        viewModelScope.launch {
            repository.deleteOldLogs(System.currentTimeMillis() - olderThanMillis)
        }
    }

    fun updateAppPermission(entry: AppPermissionEntry) {
        viewModelScope.launch { repository.updateAppPermission(entry) }
    }

    fun addBlockedKeyword(keyword: String) {
        if (keyword.isBlank()) return
        viewModelScope.launch {
            repository.insertBlockedKeyword(BlockedKeyword(keyword = keyword.trim()))
        }
    }

    fun deleteBlockedKeyword(keyword: BlockedKeyword) {
        viewModelScope.launch { repository.deleteBlockedKeyword(keyword) }
    }
}
