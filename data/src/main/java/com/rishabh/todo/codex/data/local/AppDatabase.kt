package com.rishabh.todo.codex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        NotificationEntity::class,
        ContactEntity::class,
        LearningEventEntity::class,
        KeywordRuleEntity::class,
        AnalyticsSnapshotEntity::class,
        CalendarLinkEntity::class,
        ExportJobEntity::class,
        AppSettingsEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun notificationDao(): NotificationDao
    abstract fun contactDao(): ContactDao
    abstract fun learningDao(): LearningDao
    abstract fun keywordRuleDao(): KeywordRuleDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun settingsDao(): SettingsDao
}
