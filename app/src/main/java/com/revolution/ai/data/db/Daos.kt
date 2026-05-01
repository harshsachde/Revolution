package com.revolution.ai.data.db

import androidx.room.*
import com.revolution.ai.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionLogDao {
    @Query("SELECT * FROM action_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActionLog>>

    @Query("SELECT * FROM action_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<ActionLog>>

    @Query("SELECT * FROM action_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getLogsSince(since: Long): List<ActionLog>

    @Insert
    suspend fun insert(log: ActionLog)

    @Query("DELETE FROM action_logs WHERE timestamp < :before")
    suspend fun deleteOldLogs(before: Long)
}

@Dao
interface LearnedBehaviorDao {
    @Query("SELECT * FROM learned_behaviors ORDER BY frequency DESC")
    fun getAll(): Flow<List<LearnedBehavior>>

    @Query("SELECT * FROM learned_behaviors WHERE pattern LIKE '%' || :query || '%' ORDER BY frequency DESC LIMIT 5")
    suspend fun findMatching(query: String): List<LearnedBehavior>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(behavior: LearnedBehavior)

    @Update
    suspend fun update(behavior: LearnedBehavior)

    @Delete
    suspend fun delete(behavior: LearnedBehavior)
}

@Dao
interface UrgencyRuleDao {
    @Query("SELECT * FROM urgency_rules")
    fun getAll(): Flow<List<UrgencyRule>>

    @Query("SELECT * FROM urgency_rules WHERE isEnabled = 1")
    suspend fun getActiveRules(): List<UrgencyRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: UrgencyRule)

    @Update
    suspend fun update(rule: UrgencyRule)

    @Delete
    suspend fun delete(rule: UrgencyRule)
}

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts")
    fun getAll(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts WHERE alwaysAlert = 1")
    suspend fun getAlwaysAlertContacts(): List<EmergencyContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: EmergencyContact)

    @Delete
    suspend fun delete(contact: EmergencyContact)
}

@Dao
interface AppPermissionDao {
    @Query("SELECT * FROM app_permissions ORDER BY appName ASC")
    fun getAll(): Flow<List<AppPermissionEntry>>

    @Query("SELECT * FROM app_permissions WHERE isAllowed = 1")
    suspend fun getAllowedApps(): List<AppPermissionEntry>

    @Query("SELECT * FROM app_permissions WHERE packageName = :pkg")
    suspend fun getByPackage(pkg: String): AppPermissionEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AppPermissionEntry)

    @Update
    suspend fun update(entry: AppPermissionEntry)
}

@Dao
interface BlockedKeywordDao {
    @Query("SELECT * FROM blocked_keywords")
    fun getAll(): Flow<List<BlockedKeyword>>

    @Query("SELECT * FROM blocked_keywords WHERE isActive = 1")
    suspend fun getActiveKeywords(): List<BlockedKeyword>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keyword: BlockedKeyword)

    @Delete
    suspend fun delete(keyword: BlockedKeyword)

    @Update
    suspend fun update(keyword: BlockedKeyword)
}

@Dao
interface ScheduledTaskDao {
    @Query("SELECT * FROM scheduled_tasks WHERE isCompleted = 0 ORDER BY scheduledTime ASC")
    fun getPendingTasks(): Flow<List<ScheduledTask>>

    @Query("SELECT * FROM scheduled_tasks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ScheduledTask>>

    @Query("SELECT * FROM scheduled_tasks WHERE isCompleted = 0 AND scheduledTime <= :time")
    suspend fun getDueTasks(time: Long): List<ScheduledTask>

    @Insert
    suspend fun insert(task: ScheduledTask): Long

    @Update
    suspend fun update(task: ScheduledTask)

    @Delete
    suspend fun delete(task: ScheduledTask)
}
