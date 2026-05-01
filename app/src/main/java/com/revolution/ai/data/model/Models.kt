package com.revolution.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_logs")
data class ActionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val command: String,
    val action: String,
    val result: String,
    val wasSuccessful: Boolean,
    val category: String = "general"
)

@Entity(tableName = "learned_behaviors")
data class LearnedBehavior(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val response: String,
    val frequency: Int = 1,
    val lastUsed: Long = System.currentTimeMillis(),
    val tag: String = "general" // "Work" or "Personal"
)

@Entity(tableName = "urgency_rules")
data class UrgencyRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String = "",
    val appPackage: String = "",
    val keyword: String = "",
    val threshold: Int = 3,
    val timeWindowMinutes: Int = 5,
    val isEnabled: Boolean = true
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val alwaysAlert: Boolean = true
)

@Entity(tableName = "app_permissions")
data class AppPermissionEntry(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isAllowed: Boolean = true,
    val iconUri: String? = null
)

@Entity(tableName = "blocked_keywords")
data class BlockedKeyword(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val isActive: Boolean = true
)

@Entity(tableName = "scheduled_tasks")
data class ScheduledTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val scheduledTime: Long,
    val timezone: String = "UTC",
    val isRecurring: Boolean = false,
    val recurringInterval: Long = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class VoiceOption(
    val id: String,
    val name: String,
    val gender: String,
    val locale: String,
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f
)

data class CommandResult(
    val success: Boolean,
    val message: String,
    val action: String = "",
    val requiresConfirmation: Boolean = false,
    val pendingAction: (() -> Unit)? = null
)

enum class InteractionMode {
    VOICE, NOTIFICATION, BOTH
}

enum class AssistantState {
    IDLE, LISTENING, PROCESSING, SPEAKING, ERROR
}
