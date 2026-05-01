package com.revolution.ai.ui

import android.app.Application
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.revolution.ai.data.db.AppDatabase
import com.revolution.ai.data.model.*
import com.revolution.ai.data.preferences.UserPreferences
import com.revolution.ai.data.repository.AppRepository
import com.revolution.ai.engine.command.CommandExecutor
import com.revolution.ai.engine.command.CommandParser
import com.revolution.ai.engine.command.CommandType
import com.revolution.ai.engine.learning.LearningEngine
import com.revolution.ai.service.VoiceAssistantService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val commandParser = CommandParser()
    private val commandExecutor = CommandExecutor(application)
    private val learningEngine = LearningEngine(repository)

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

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

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setLanguage(Locale.US)
                ttsReady = true
                applyVoiceSettings()
            }
        }
    }

    private fun applyVoiceSettings() {
        tts?.setPitch(voicePitch.value)
        tts?.setSpeechRate(voiceSpeed.value)
    }

    fun previewVoice() {
        applyVoiceSettings()
        if (ttsReady) {
            tts?.speak(
                "Hello! I am ${aiName.value}, your voice assistant.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "preview_${System.currentTimeMillis()}"
            )
        }
    }

    fun setAssistantState(state: AssistantState) {
        _assistantState.value = state
    }

    fun setLastResponse(message: String) {
        _lastResponse.value = message
    }

    fun toggleListening() {
        when (_assistantState.value) {
            AssistantState.LISTENING -> {
                _assistantState.value = AssistantState.IDLE
                VoiceAssistantService.stopService(getApplication())
            }
            AssistantState.IDLE, AssistantState.ERROR -> {
                _assistantState.value = AssistantState.LISTENING
                VoiceAssistantService.startService(getApplication())
            }
            else -> { }
        }
    }

    fun processTextCommand(command: String) {
        if (command.isBlank()) return

        _assistantState.value = AssistantState.PROCESSING
        _lastResponse.value = "Processing: \"$command\""

        viewModelScope.launch {
            try {
                val parsed = commandParser.parse(command)

                if (parsed.type == CommandType.UNKNOWN) {
                    _lastResponse.value = "I didn't understand: \"$command\". Try saying something like \"call Mom\" or \"turn on WiFi\"."
                    _assistantState.value = AssistantState.IDLE
                    repository.insertLog(
                        ActionLog(
                            command = command,
                            action = "unknown",
                            result = "Command not recognized",
                            wasSuccessful = false,
                            category = "unknown"
                        )
                    )
                    return@launch
                }

                val allowedApps = repository.getAllowedApps()
                if (parsed.appPackage.isNotBlank()) {
                    val appPerm = repository.getAppPermission(parsed.appPackage)
                    if (appPerm != null && !appPerm.isAllowed) {
                        _lastResponse.value = "Access to ${appPerm.appName} is blocked. You can enable it in Permissions."
                        _assistantState.value = AssistantState.IDLE
                        repository.insertLog(
                            ActionLog(
                                command = command,
                                action = "blocked",
                                result = "App access denied: ${parsed.appPackage}",
                                wasSuccessful = false,
                                category = "permission"
                            )
                        )
                        return@launch
                    }
                }

                val blockedKws = repository.getActiveBlockedKeywords()
                val hasBlocked = blockedKws.any { kw ->
                    command.contains(kw.keyword, ignoreCase = true)
                }
                if (hasBlocked) {
                    _lastResponse.value = "This command contains a blocked keyword and was not executed."
                    _assistantState.value = AssistantState.IDLE
                    repository.insertLog(
                        ActionLog(
                            command = command,
                            action = "blocked",
                            result = "Blocked keyword detected",
                            wasSuccessful = false,
                            category = "permission"
                        )
                    )
                    return@launch
                }

                val result = withContext(Dispatchers.Main) {
                    commandExecutor.execute(parsed)
                }

                repository.insertLog(
                    ActionLog(
                        command = command,
                        action = result.action,
                        result = result.message,
                        wasSuccessful = result.success,
                        category = parsed.type.name.lowercase()
                    )
                )

                learningEngine.learn(command = command, action = result.action)

                _lastResponse.value = result.message

                if (result.requiresConfirmation && result.pendingAction != null) {
                    _lastResponse.value = "${result.message} — executing..."
                    withContext(Dispatchers.Main) {
                        result.pendingAction.invoke()
                    }
                }

                if (ttsReady && interactionMode.value != "NOTIFICATION") {
                    withContext(Dispatchers.Main) {
                        tts?.speak(
                            result.message,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "cmd_${System.currentTimeMillis()}"
                        )
                    }
                }

                _assistantState.value = AssistantState.IDLE

            } catch (e: Exception) {
                val errorMsg = "Error: ${e.localizedMessage ?: "Something went wrong"}"
                _lastResponse.value = errorMsg
                _assistantState.value = AssistantState.ERROR

                repository.insertLog(
                    ActionLog(
                        command = command,
                        action = "error",
                        result = errorMsg,
                        wasSuccessful = false,
                        category = "error"
                    )
                )

                delay(2000)
                _assistantState.value = AssistantState.IDLE
            }
        }
    }

    fun <T> updatePreference(key: androidx.datastore.preferences.core.Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            prefs.update(key, value)
            if (key == UserPreferences.VOICE_PITCH || key == UserPreferences.VOICE_SPEED) {
                applyVoiceSettings()
            }
        }
    }

    fun deleteOldLogs(olderThanMillis: Long) {
        viewModelScope.launch {
            repository.deleteOldLogs(System.currentTimeMillis() - olderThanMillis)
        }
    }

    fun updateAppPermission(entry: AppPermissionEntry) {
        viewModelScope.launch { repository.updateAppPermission(entry) }
    }

    fun insertAppPermission(entry: AppPermissionEntry) {
        viewModelScope.launch { repository.insertAppPermission(entry) }
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

    fun exportLogsToEmail() {
        viewModelScope.launch {
            val logs = repository.getLogsSince(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
            val body = buildString {
                appendLine("Revolution AI - Daily Action Summary")
                appendLine("=" .repeat(40))
                appendLine()
                logs.forEach { log ->
                    appendLine("[${java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date(log.timestamp))}] ${log.command}")
                    appendLine("  Action: ${log.action} | Result: ${log.result} | ${if (log.wasSuccessful) "OK" else "FAILED"}")
                    appendLine()
                }
                if (logs.isEmpty()) {
                    appendLine("No actions recorded in the last 24 hours.")
                }
            }

            val email = logEmail.value
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:${android.net.Uri.encode(email)}")
                putExtra(Intent.EXTRA_SUBJECT, "Revolution AI - Daily Summary")
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                getApplication<Application>().startActivity(intent)
                _lastResponse.value = "Opening email to send daily summary"
            } catch (e: Exception) {
                _lastResponse.value = "No email app found"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
