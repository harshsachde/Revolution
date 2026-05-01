package com.revolution.ai.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.revolution.ai.R
import com.revolution.ai.RevolutionApp
import com.revolution.ai.data.db.AppDatabase
import com.revolution.ai.data.model.ActionLog
import com.revolution.ai.data.repository.AppRepository
import com.revolution.ai.service.VoiceAssistantService
import com.revolution.ai.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_COMMAND = "extra_command"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val command = intent.getStringExtra(EXTRA_COMMAND) ?: ""

        scope.launch {
            try {
                val database = AppDatabase.getInstance(context)
                val repository = AppRepository(database)

                if (taskId > 0) {
                    val dueTasks = repository.getDueTasks(System.currentTimeMillis())
                    val task = dueTasks.find { it.id == taskId }

                    if (task != null && !task.isCompleted) {
                        VoiceAssistantService.sendTextCommand(context, task.command)

                        repository.updateTask(task.copy(isCompleted = !task.isRecurring))

                        repository.insertLog(
                            ActionLog(
                                command = task.command,
                                action = "scheduled_execution",
                                result = "Scheduled task executed: ${task.command}",
                                wasSuccessful = true,
                                category = "schedule"
                            )
                        )

                        showTaskNotification(context, task.command)
                    }
                } else if (command.isNotBlank()) {
                    VoiceAssistantService.sendTextCommand(context, command)
                    showTaskNotification(context, command)
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showTaskNotification(context: Context, command: String) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, RevolutionApp.CHANNEL_GENERAL)
            .setContentTitle("Scheduled Task")
            .setContentText("Executing: $command")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify("task_${command.hashCode()}".hashCode(), notification)
    }
}
