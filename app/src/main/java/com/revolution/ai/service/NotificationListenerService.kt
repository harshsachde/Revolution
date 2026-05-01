package com.revolution.ai.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.revolution.ai.R
import com.revolution.ai.RevolutionApp
import com.revolution.ai.data.db.AppDatabase
import com.revolution.ai.data.preferences.UserPreferences
import com.revolution.ai.data.repository.AppRepository
import com.revolution.ai.engine.urgency.UrgencyEngine
import com.revolution.ai.engine.urgency.UrgencyEvent
import com.revolution.ai.engine.urgency.UrgencyLevel
import com.revolution.ai.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class NotificationListenerService :
    android.service.notification.NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repository: AppRepository
    private lateinit var preferences: UserPreferences
    private val urgencyEngine = UrgencyEngine()

    private val ignoredPackages = setOf(
        "com.revolution.ai",
        "com.android.systemui",
        "android",
        "com.android.providers.downloads"
    )

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = AppRepository(database)
        preferences = UserPreferences(this)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName in ignoredPackages) return
        if (!sbn.isClearable) return

        serviceScope.launch {
            try {
                val urgencyEnabled = preferences.urgencyEnabled.first()
                if (!urgencyEnabled) return@launch

                val extras = sbn.notification.extras ?: return@launch
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

                if (title.isBlank() && text.isBlank()) return@launch

                val blockedKeywords = repository.getActiveBlockedKeywords()
                val fullText = "$title $text".lowercase()
                val isBlocked = blockedKeywords.any { keyword ->
                    fullText.contains(keyword.keyword.lowercase())
                }
                if (isBlocked) return@launch

                val event = UrgencyEvent(
                    sender = title,
                    message = text,
                    app = sbn.packageName,
                    timestamp = sbn.postTime
                )

                val rules = repository.getActiveUrgencyRules()
                val emergencyContacts = repository.getAlwaysAlertContacts()

                val assessment = urgencyEngine.assess(event, rules, emergencyContacts)

                if (assessment.shouldAlert) {
                    showUrgentNotification(title, text, assessment.level, assessment.reason)
                }

                if (assessment.shouldCallName) {
                    broadcastUrgentAlert(title, text, assessment.reason)
                }
            } catch (_: Exception) {
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No-op
    }

    private fun showUrgentNotification(
        sender: String,
        message: String,
        level: UrgencyLevel,
        reason: String
    ) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when (level) {
            UrgencyLevel.CRITICAL -> NotificationCompat.PRIORITY_MAX
            UrgencyLevel.HIGH -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(this, RevolutionApp.CHANNEL_URGENT)
            .setContentTitle("Urgent: $sender")
            .setContentText(message)
            .setSubText(reason)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\nReason: $reason"))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            "urgent_${sender.hashCode()}".hashCode(),
            notification
        )
    }

    private fun broadcastUrgentAlert(sender: String, message: String, reason: String) {
        sendBroadcast(Intent(VoiceAssistantService.ACTION_RESPONSE).apply {
            setPackage(packageName)
            putExtra(
                VoiceAssistantService.EXTRA_MESSAGE,
                "Urgent message from $sender: $message"
            )
        })

        val serviceIntent = Intent(this, VoiceAssistantService::class.java).apply {
            putExtra(
                VoiceAssistantService.EXTRA_TEXT_COMMAND,
                "__URGENT_ALERT__:$sender:$message"
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
