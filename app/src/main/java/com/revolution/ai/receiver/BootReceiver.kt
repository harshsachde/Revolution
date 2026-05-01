package com.revolution.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.revolution.ai.data.preferences.UserPreferences
import com.revolution.ai.service.VoiceAssistantService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val preferences = UserPreferences(context)
                val alwaysListening = preferences.isAlwaysListening.first()
                if (alwaysListening) {
                    VoiceAssistantService.startService(context)
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
