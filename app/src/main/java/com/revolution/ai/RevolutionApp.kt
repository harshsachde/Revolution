package com.revolution.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.revolution.ai.data.db.AppDatabase

class RevolutionApp : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE, "Voice Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Keeps the voice assistant running" }

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT, "Urgent Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent message alerts"
                enableVibration(true)
                enableLights(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL, "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "General assistant notifications" }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(urgentChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    companion object {
        const val CHANNEL_SERVICE = "revolution_service"
        const val CHANNEL_URGENT = "revolution_urgent"
        const val CHANNEL_GENERAL = "revolution_general"
    }
}
