package com.rishabh.todo.codex.domain.model

import java.time.Instant
import kotlinx.serialization.Serializable

enum class SourceType {
    WHATSAPP,
    GMAIL,
    TELEGRAM,
    SLACK,
    SMS,
    CALENDAR,
    GENERIC,
}

enum class TaskPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
}

enum class TaskStatus {
    PENDING,
    COMPLETED,
    ARCHIVED,
    DELETED,
}

enum class ReminderMode {
    AGGRESSIVE,
    BALANCED,
    MINIMAL,
}

enum class ContactTrust {
    VIP,
    HIGH_PRIORITY,
    NORMAL,
    IGNORE,
}

enum class TaskCreationDecision {
    AUTO_CREATE,
    INBOX_REVIEW,
    IGNORE,
}

enum class LearningEventType {
    APPROVED,
    REJECTED,
    COMPLETED,
    IGNORED,
    MANUALLY_EDITED,
}

@Serializable
data class NotificationRecord(
    val id: Long = 0L,
    val packageName: String,
    val sourceType: SourceType,
    val sender: String?,
    val title: String?,
    val body: String?,
    val conversationType: String?,
    val rawText: String,
    val receivedAtEpochMillis: Long,
)

@Serializable
data class ExtractionReason(
    val matchedKeywords: List<String> = emptyList(),
    val inferredDatePhrase: String? = null,
    val modelScore: Float = 0f,
    val senderWeight: Float = 0f,
    val threshold: Float = 0f,
)

@Serializable
data class ExtractionResult(
    val actionable: Boolean,
    val title: String,
    val description: String,
    val priority: TaskPriority,
    val dueAtEpochMillis: Long?,
    val decision: TaskCreationDecision,
    val confidence: Float,
    val sender: String?,
    val sourceType: SourceType,
    val reason: ExtractionReason,
)

@Serializable
data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val sourceType: SourceType,
    val sender: String?,
    val createdAtEpochMillis: Long = Instant.now().toEpochMilli(),
    val dueAtEpochMillis: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val originalNotificationText: String = "",
    val transcriptDerived: Boolean = false,
    val ownerLabel: String? = null,
    val reminderState: String = "ACTIVE",
    val linkedCalendarEventId: Long? = null,
    val confidence: Float = 0f,
)

@Serializable
data class ContactProfile(
    val id: Long = 0L,
    val displayName: String,
    val trust: ContactTrust = ContactTrust.NORMAL,
    val learnedWeight: Float = 0.5f,
)

@Serializable
data class KeywordRule(
    val id: Long = 0L,
    val phrase: String,
    val category: String,
    val weight: Float,
    val languageHint: String,
    val enabled: Boolean = true,
)

@Serializable
data class LearningEvent(
    val id: Long = 0L,
    val taskId: Long?,
    val notificationId: Long?,
    val eventType: LearningEventType,
    val payload: String,
    val createdAtEpochMillis: Long = Instant.now().toEpochMilli(),
)

@Serializable
data class AnalyticsSnapshot(
    val completedToday: Int,
    val completedWeek: Int,
    val completedMonth: Int,
    val pendingBacklog: Int,
    val completionRate: Float,
    val ignoredToday: Int,
    val mostCommonSource: String,
)

@Serializable
data class TranscriptCandidateTask(
    val owner: String?,
    val title: String,
    val description: String,
    val dueAtEpochMillis: Long?,
    val priority: TaskPriority,
    val confidence: Float,
)

data class ReminderPolicy(
    val mode: ReminderMode,
    val intervalMinutes: Long,
)

data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val reminderMode: ReminderMode = ReminderMode.BALANCED,
    val reminderIntervalMinutes: Long = 30L,
    val emailReportHour: Int = 20,
    val dailyReportEnabled: Boolean = true,
    val biometricLockEnabled: Boolean = false,
    val scheduledExportEnabled: Boolean = false,
    val scheduledExportPath: String? = null,
    val scheduledExportFormat: String = "json",
)
