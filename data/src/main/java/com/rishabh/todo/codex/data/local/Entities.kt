package com.rishabh.todo.codex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val text: String,
    val rawSourceText: String,
    val sourceApp: String,
    val sourceAppDisplay: String,
    val sender: String?,
    val createdAt: Long,
    val dueAt: Long?,
    val completedAt: Long?,
    val priority: String,
    val status: String,
    val triggerKeywordsJson: String,
    val confidence: Float,
    val needsConfirmation: Boolean,
    val calendarEventId: Long?,
    val language: String,
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val sourceAppDisplay: String,
    val sender: String?,
    val title: String?,
    val body: String?,
    val conversationType: String?,
    val rawText: String,
    val receivedAtEpochMillis: Long,
    val decision: String = "NEW",
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val displayName: String,
    val trust: String,
    val learnedWeight: Float,
)

@Entity(tableName = "learning_events")
data class LearningEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: String?,
    val notificationId: Long?,
    val eventType: String,
    val payload: String,
    val createdAtEpochMillis: Long,
)

@Entity(tableName = "keyword_rules")
data class KeywordRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val phrase: String,
    val category: String,
    val weight: Float,
    val languageHint: String,
    val enabled: Boolean,
)

@Entity(tableName = "analytics_snapshots")
data class AnalyticsSnapshotEntity(
    @PrimaryKey val singletonId: Int = 1,
    val completedToday: Int,
    val completedWeek: Int,
    val completedMonth: Int,
    val pendingBacklog: Int,
    val completionRate: Float,
    val ignoredToday: Int,
    val mostCommonSource: String,
)

@Entity(tableName = "calendar_links")
data class CalendarLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: String,
    val calendarEventId: Long,
)

@Entity(tableName = "export_jobs")
data class ExportJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val format: String,
    val destinationPath: String,
    val scheduledAtEpochMillis: Long,
    val enabled: Boolean,
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val singletonId: Int = 1,
    val onboardingCompleted: Boolean,
    val reminderMode: String,
    val reminderIntervalMinutes: Long,
    val emailReportHour: Int,
    val dailyReportEnabled: Boolean,
    val biometricLockEnabled: Boolean,
    val scheduledExportEnabled: Boolean,
    val scheduledExportPath: String?,
    val scheduledExportFormat: String,
)
