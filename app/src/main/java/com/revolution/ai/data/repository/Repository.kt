package com.revolution.ai.data.repository

import com.revolution.ai.data.db.AppDatabase
import com.revolution.ai.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    // Action Logs
    fun getAllLogs(): Flow<List<ActionLog>> = db.actionLogDao().getAllLogs()
    fun getRecentLogs(limit: Int = 50): Flow<List<ActionLog>> = db.actionLogDao().getRecentLogs(limit)
    suspend fun getLogsSince(since: Long): List<ActionLog> = db.actionLogDao().getLogsSince(since)
    suspend fun insertLog(log: ActionLog) = db.actionLogDao().insert(log)
    suspend fun deleteOldLogs(before: Long) = db.actionLogDao().deleteOldLogs(before)

    // Learned Behaviors
    fun getAllBehaviors(): Flow<List<LearnedBehavior>> = db.learnedBehaviorDao().getAll()
    suspend fun findMatchingBehaviors(query: String): List<LearnedBehavior> = db.learnedBehaviorDao().findMatching(query)
    suspend fun insertBehavior(behavior: LearnedBehavior) = db.learnedBehaviorDao().insert(behavior)
    suspend fun updateBehavior(behavior: LearnedBehavior) = db.learnedBehaviorDao().update(behavior)

    // Urgency Rules
    fun getAllUrgencyRules(): Flow<List<UrgencyRule>> = db.urgencyRuleDao().getAll()
    suspend fun getActiveUrgencyRules(): List<UrgencyRule> = db.urgencyRuleDao().getActiveRules()
    suspend fun insertUrgencyRule(rule: UrgencyRule) = db.urgencyRuleDao().insert(rule)
    suspend fun updateUrgencyRule(rule: UrgencyRule) = db.urgencyRuleDao().update(rule)
    suspend fun deleteUrgencyRule(rule: UrgencyRule) = db.urgencyRuleDao().delete(rule)

    // Emergency Contacts
    fun getAllEmergencyContacts(): Flow<List<EmergencyContact>> = db.emergencyContactDao().getAll()
    suspend fun getAlwaysAlertContacts(): List<EmergencyContact> = db.emergencyContactDao().getAlwaysAlertContacts()
    suspend fun insertEmergencyContact(contact: EmergencyContact) = db.emergencyContactDao().insert(contact)
    suspend fun deleteEmergencyContact(contact: EmergencyContact) = db.emergencyContactDao().delete(contact)

    // App Permissions
    fun getAllAppPermissions(): Flow<List<AppPermissionEntry>> = db.appPermissionDao().getAll()
    suspend fun getAllowedApps(): List<AppPermissionEntry> = db.appPermissionDao().getAllowedApps()
    suspend fun getAppPermission(pkg: String): AppPermissionEntry? = db.appPermissionDao().getByPackage(pkg)
    suspend fun insertAppPermission(entry: AppPermissionEntry) = db.appPermissionDao().insert(entry)
    suspend fun updateAppPermission(entry: AppPermissionEntry) = db.appPermissionDao().update(entry)

    // Blocked Keywords
    fun getAllBlockedKeywords(): Flow<List<BlockedKeyword>> = db.blockedKeywordDao().getAll()
    suspend fun getActiveBlockedKeywords(): List<BlockedKeyword> = db.blockedKeywordDao().getActiveKeywords()
    suspend fun insertBlockedKeyword(keyword: BlockedKeyword) = db.blockedKeywordDao().insert(keyword)
    suspend fun deleteBlockedKeyword(keyword: BlockedKeyword) = db.blockedKeywordDao().delete(keyword)
    suspend fun updateBlockedKeyword(keyword: BlockedKeyword) = db.blockedKeywordDao().update(keyword)

    // Scheduled Tasks
    fun getPendingTasks(): Flow<List<ScheduledTask>> = db.scheduledTaskDao().getPendingTasks()
    fun getAllTasks(): Flow<List<ScheduledTask>> = db.scheduledTaskDao().getAll()
    suspend fun getDueTasks(time: Long): List<ScheduledTask> = db.scheduledTaskDao().getDueTasks(time)
    suspend fun insertTask(task: ScheduledTask): Long = db.scheduledTaskDao().insert(task)
    suspend fun updateTask(task: ScheduledTask) = db.scheduledTaskDao().update(task)
    suspend fun deleteTask(task: ScheduledTask) = db.scheduledTaskDao().delete(task)
}
