package com.revolution.ai.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "revolution_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val WAKE_WORD = stringPreferencesKey("wake_word")
        val AI_NAME = stringPreferencesKey("ai_name")
        val APP_NAME = stringPreferencesKey("app_name")
        val VOICE_ID = stringPreferencesKey("voice_id")
        val VOICE_PITCH = floatPreferencesKey("voice_pitch")
        val VOICE_SPEED = floatPreferencesKey("voice_speed")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val ALWAYS_LISTENING = booleanPreferencesKey("always_listening")
        val INTERACTION_MODE = stringPreferencesKey("interaction_mode")
        val LOG_EMAIL = stringPreferencesKey("log_email")
        val DAILY_SUMMARY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
        val WORK_MODE = booleanPreferencesKey("work_mode")
        val CLOUD_SYNC = booleanPreferencesKey("cloud_sync")
        val WORK_WHEN_LOCKED = booleanPreferencesKey("work_when_locked")
        val URGENCY_ENABLED = booleanPreferencesKey("urgency_enabled")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val CUSTOM_ICON_URI = stringPreferencesKey("custom_icon_uri")
    }

    val wakeWord: Flow<String> = context.dataStore.data.map { it[WAKE_WORD] ?: "Nikhil" }
    val aiName: Flow<String> = context.dataStore.data.map { it[AI_NAME] ?: "Revolution" }
    val appName: Flow<String> = context.dataStore.data.map { it[APP_NAME] ?: "Revolution" }
    val voiceId: Flow<String> = context.dataStore.data.map { it[VOICE_ID] ?: "default" }
    val voicePitch: Flow<Float> = context.dataStore.data.map { it[VOICE_PITCH] ?: 1.0f }
    val voiceSpeed: Flow<Float> = context.dataStore.data.map { it[VOICE_SPEED] ?: 1.0f }
    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[DARK_THEME] ?: true }
    val isAlwaysListening: Flow<Boolean> = context.dataStore.data.map { it[ALWAYS_LISTENING] ?: false }
    val interactionMode: Flow<String> = context.dataStore.data.map { it[INTERACTION_MODE] ?: "VOICE" }
    val logEmail: Flow<String> = context.dataStore.data.map { it[LOG_EMAIL] ?: "hnamproject@gmail.com" }
    val dailySummaryEnabled: Flow<Boolean> = context.dataStore.data.map { it[DAILY_SUMMARY_ENABLED] ?: true }
    val isWorkMode: Flow<Boolean> = context.dataStore.data.map { it[WORK_MODE] ?: false }
    val cloudSync: Flow<Boolean> = context.dataStore.data.map { it[CLOUD_SYNC] ?: false }
    val workWhenLocked: Flow<Boolean> = context.dataStore.data.map { it[WORK_WHEN_LOCKED] ?: false }
    val urgencyEnabled: Flow<Boolean> = context.dataStore.data.map { it[URGENCY_ENABLED] ?: true }

    suspend fun <T> update(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }
}
