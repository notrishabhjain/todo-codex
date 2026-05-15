package com.rishabh.todo.codex.domain.model

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}



enum class TaskPriority {
    URGENT,
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
    val sourceAppDisplay: String,
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
    val sourceApp: String,
    val sourceAppDisplay: String,
    val reason: ExtractionReason,
)

@Serializable
data class Task(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val text: String,
    val rawSourceText: String,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val sourceApp: String,
    val sourceAppDisplay: String,
    val sender: String?,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val completedAt: Long? = null,
    val dueAt: Long? = null,
    val triggerKeywords: List<String> = emptyList(),
    val confidence: Float = 0f,
    val needsConfirmation: Boolean = false,
    val calendarEventId: Long? = null,
    val language: String = "en",
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
    val taskId: String?,
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
