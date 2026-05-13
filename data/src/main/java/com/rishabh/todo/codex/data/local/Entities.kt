package com.rishabh.todo.codex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val sourceType: String,
    val sender: String?,
    val createdAtEpochMillis: Long,
    val dueAtEpochMillis: Long?,
    val priority: String,
    val status: String,
    val tagsJson: String,
    val notes: String,
    val originalNotificationText: String,
    val transcriptDerived: Boolean,
    val ownerLabel: String?,
    val reminderState: String,
    val linkedCalendarEventId: Long?,
    val confidence: Float,
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val sourceType: String,
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
    val taskId: Long?,
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
    val taskId: Long,
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
    val reminderMode: String,
    val reminderIntervalMinutes: Long,
    val emailReportHour: Int,
    val biometricLockEnabled: Boolean,
    val scheduledExportEnabled: Boolean,
    val scheduledExportPath: String?,
)
