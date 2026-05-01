package com.revolution.ai.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import com.revolution.ai.R
import com.revolution.ai.data.db.AppDatabase
import com.revolution.ai.data.model.*
import com.revolution.ai.data.preferences.UserPreferences
import com.revolution.ai.data.repository.AppRepository
import com.revolution.ai.engine.command.CommandExecutor
import com.revolution.ai.engine.command.CommandParser
import com.revolution.ai.engine.learning.LearningEngine
import com.revolution.ai.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Locale

class VoiceAssistantService : Service(), TextToSpeech.OnInitListener {

    companion object {
        const val ACTION_STATE_CHANGED = "com.revolution.ai.STATE_CHANGED"
        const val ACTION_RESPONSE = "com.revolution.ai.RESPONSE"
        const val EXTRA_STATE = "extra_state"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_TEXT_COMMAND = "extra_text_command"
        const val EXTRA_SPOKEN_TEXT = "extra_spoken_text"

        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "revolution_service"

        fun startService(context: Context) {
            val intent = Intent(context, VoiceAssistantService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, VoiceAssistantService::class.java))
        }

        fun sendTextCommand(context: Context, command: String) {
            val intent = Intent(context, VoiceAssistantService::class.java).apply {
                putExtra(EXTRA_TEXT_COMMAND, command)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var preferences: UserPreferences
    private lateinit var repository: AppRepository
    private lateinit var commandParser: CommandParser
    private lateinit var commandExecutor: CommandExecutor
    private lateinit var learningEngine: LearningEngine

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private var currentState = AssistantState.IDLE
    private var wakeWord = "Nikhil"
    private var isAlwaysListening = false
    private var wakeWordDetected = false
    private var voicePitch = 1.0f
    private var voiceSpeed = 1.0f

    override fun onCreate() {
        super.onCreate()

        val database = AppDatabase.getInstance(this)
        preferences = UserPreferences(this)
        repository = AppRepository(database)
        commandParser = CommandParser()
        commandExecutor = CommandExecutor(this)
        learningEngine = LearningEngine(repository)

        tts = TextToSpeech(this, this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Initializing..."))

        loadPreferences()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_TEXT_COMMAND)?.let { textCommand ->
            if (textCommand.isNotBlank()) {
                processCommand(textCommand)
                return START_STICKY
            }
        }

        serviceScope.launch {
            isAlwaysListening = preferences.isAlwaysListening.first()
            if (isAlwaysListening) {
                startWakeWordListening()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        speechRecognizer = null
        tts?.apply {
            stop()
            shutdown()
        }
        tts = null
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_AVAILABLE
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    updateState(AssistantState.SPEAKING)
                }

                override fun onDone(utteranceId: String?) {
                    updateState(AssistantState.IDLE)
                    if (isAlwaysListening) {
                        serviceScope.launch(Dispatchers.Main) {
                            delay(300)
                            startWakeWordListening()
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    updateState(AssistantState.IDLE)
                    if (isAlwaysListening) {
                        serviceScope.launch(Dispatchers.Main) {
                            delay(300)
                            startWakeWordListening()
                        }
                    }
                }
            })
            applyVoiceSettings()
        } else {
            ttsReady = false
        }
    }

    private fun loadPreferences() {
        serviceScope.launch {
            launch {
                preferences.wakeWord.collect { word ->
                    wakeWord = word
                }
            }
            launch {
                preferences.isAlwaysListening.collect { listening ->
                    val wasListening = isAlwaysListening
                    isAlwaysListening = listening
                    if (listening && !wasListening) {
                        startWakeWordListening()
                    } else if (!listening && wasListening) {
                        stopListening()
                    }
                }
            }
            launch {
                preferences.voicePitch.collect { pitch ->
                    voicePitch = pitch
                    applyVoiceSettings()
                }
            }
            launch {
                preferences.voiceSpeed.collect { speed ->
                    voiceSpeed = speed
                    applyVoiceSettings()
                }
            }
        }
    }

    private fun applyVoiceSettings() {
        tts?.setPitch(voicePitch)
        tts?.setSpeechRate(voiceSpeed)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the voice assistant running in the background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Revolution AI")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    private fun updateState(state: AssistantState) {
        currentState = state
        val statusText = when (state) {
            AssistantState.IDLE -> if (isAlwaysListening) "Listening for wake word" else "Idle"
            AssistantState.LISTENING -> if (wakeWordDetected) "Listening for command..." else "Listening for \"$wakeWord\"..."
            AssistantState.PROCESSING -> "Processing command..."
            AssistantState.SPEAKING -> "Speaking..."
            AssistantState.ERROR -> "Error occurred"
        }
        updateNotification(statusText)
        sendBroadcast(Intent(ACTION_STATE_CHANGED).apply {
            setPackage(packageName)
            putExtra(EXTRA_STATE, state.name)
        })
    }

    private fun sendResponse(message: String) {
        sendBroadcast(Intent(ACTION_RESPONSE).apply {
            setPackage(packageName)
            putExtra(EXTRA_MESSAGE, message)
        })
    }

    private fun startWakeWordListening() {
        wakeWordDetected = false
        startListening()
    }

    private fun startCommandListening() {
        wakeWordDetected = true
        startListening()
    }

    private fun startListening() {
        if (currentState == AssistantState.SPEAKING) return

        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            updateState(AssistantState.ERROR)
            sendResponse("Speech recognition is not available on this device.")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(createRecognitionListener())
        }

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            if (!wakeWordDetected) {
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)
            }
        }

        try {
            speechRecognizer?.startListening(recognizerIntent)
            updateState(AssistantState.LISTENING)
        } catch (e: SecurityException) {
            updateState(AssistantState.ERROR)
            sendResponse("Microphone permission is required.")
        } catch (e: Exception) {
            updateState(AssistantState.ERROR)
            scheduleListeningRestart()
        }
    }

    private fun stopListening() {
        speechRecognizer?.apply {
            stopListening()
            cancel()
        }
        wakeWordDetected = false
        updateState(AssistantState.IDLE)
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                updateState(AssistantState.LISTENING)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                val shouldRestart = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> true
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        speechRecognizer?.cancel()
                        true
                    }
                    SpeechRecognizer.ERROR_CLIENT -> false
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> true
                    SpeechRecognizer.ERROR_SERVER -> true
                    SpeechRecognizer.ERROR_AUDIO,
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        updateState(AssistantState.ERROR)
                        false
                    }
                    else -> true
                }

                if (shouldRestart && isAlwaysListening) {
                    scheduleListeningRestart()
                } else if (!isAlwaysListening) {
                    updateState(AssistantState.IDLE)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.filterNotNull()

                if (matches.isNullOrEmpty()) {
                    if (isAlwaysListening) scheduleListeningRestart()
                    return
                }

                val spokenText = matches.first()

                sendBroadcast(Intent(ACTION_STATE_CHANGED).apply {
                    setPackage(packageName)
                    putExtra(EXTRA_STATE, AssistantState.LISTENING.name)
                    putExtra(EXTRA_SPOKEN_TEXT, spokenText)
                })

                if (!wakeWordDetected) {
                    handleWakeWordDetection(spokenText, matches)
                } else {
                    processCommand(spokenText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()

                if (!partial.isNullOrBlank() && !wakeWordDetected) {
                    if (containsWakeWord(partial)) {
                        val commandAfterWake = extractCommandAfterWakeWord(partial)
                        if (commandAfterWake.isNotBlank()) {
                            speechRecognizer?.stopListening()
                        }
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun handleWakeWordDetection(spokenText: String, allMatches: List<String>) {
        val matchedText = allMatches.firstOrNull { containsWakeWord(it) }

        if (matchedText != null) {
            val commandAfterWake = extractCommandAfterWakeWord(matchedText)
            if (commandAfterWake.isNotBlank()) {
                processCommand(commandAfterWake)
            } else {
                speak("Yes? How can I help you?")
                serviceScope.launch(Dispatchers.Main) {
                    delay(2000)
                    startCommandListening()
                }
            }
        } else {
            if (isAlwaysListening) {
                scheduleListeningRestart()
            }
        }
    }

    private fun containsWakeWord(text: String): Boolean {
        return text.lowercase().contains(wakeWord.lowercase())
    }

    private fun extractCommandAfterWakeWord(text: String): String {
        val lower = text.lowercase()
        val wakeIndex = lower.indexOf(wakeWord.lowercase())
        if (wakeIndex < 0) return ""
        val afterWake = text.substring(wakeIndex + wakeWord.length).trim()
        val cleaned = afterWake.removePrefix(",").removePrefix(".").trim()
        return if (cleaned.length > 2) cleaned else ""
    }

    private fun processCommand(commandText: String) {
        updateState(AssistantState.PROCESSING)

        serviceScope.launch {
            try {
                val parsed = commandParser.parse(commandText)
                val result = withContext(Dispatchers.Main) {
                    commandExecutor.execute(parsed)
                }

                repository.insertLog(
                    ActionLog(
                        command = commandText,
                        action = result.action,
                        result = result.message,
                        wasSuccessful = result.success,
                        category = parsed.type.name.lowercase()
                    )
                )

                learningEngine.learn(
                    command = commandText,
                    action = result.action
                )

                if (result.requiresConfirmation) {
                    speak("${result.message}. Should I proceed?")
                } else {
                    speak(result.message)
                }

                sendResponse(result.message)
            } catch (e: Exception) {
                val errorMessage = "Sorry, something went wrong: ${e.localizedMessage ?: "Unknown error"}"
                speak(errorMessage)
                sendResponse(errorMessage)

                repository.insertLog(
                    ActionLog(
                        command = commandText,
                        action = "error",
                        result = errorMessage,
                        wasSuccessful = false,
                        category = "error"
                    )
                )

                updateState(AssistantState.ERROR)
            }
        }
    }

    private fun speak(text: String) {
        if (!ttsReady || tts == null) {
            updateState(AssistantState.IDLE)
            if (isAlwaysListening) {
                scheduleListeningRestart()
            }
            return
        }

        updateState(AssistantState.SPEAKING)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "utterance_${System.currentTimeMillis()}")
    }

    private fun scheduleListeningRestart() {
        serviceScope.launch(Dispatchers.Main) {
            delay(500)
            if (isAlwaysListening && currentState != AssistantState.SPEAKING) {
                startWakeWordListening()
            }
        }
    }
}
