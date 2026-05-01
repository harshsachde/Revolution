package com.revolution.ai.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.revolution.ai.data.model.*

@Database(
    entities = [
        ActionLog::class,
        LearnedBehavior::class,
        UrgencyRule::class,
        EmergencyContact::class,
        AppPermissionEntry::class,
        BlockedKeyword::class,
        ScheduledTask::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun actionLogDao(): ActionLogDao
    abstract fun learnedBehaviorDao(): LearnedBehaviorDao
    abstract fun urgencyRuleDao(): UrgencyRuleDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun appPermissionDao(): AppPermissionDao
    abstract fun blockedKeywordDao(): BlockedKeywordDao
    abstract fun scheduledTaskDao(): ScheduledTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "revolution_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
