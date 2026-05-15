package com.rishabh.todo.codex.data.local

import com.rishabh.todo.codex.domain.model.AnalyticsSnapshot
import com.rishabh.todo.codex.domain.model.AppSettings
import com.rishabh.todo.codex.domain.model.ContactProfile
import com.rishabh.todo.codex.domain.model.ContactTrust
import com.rishabh.todo.codex.domain.model.KeywordRule
import com.rishabh.todo.codex.domain.model.LearningEvent
import com.rishabh.todo.codex.domain.model.LearningEventType
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.domain.model.TaskPriority
import com.rishabh.todo.codex.domain.model.TaskStatus
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json

fun TaskEntity.toDomain() = Task(
    id = java.util.UUID.fromString(id),
    text = text,
    rawSourceText = rawSourceText,
    sourceApp = sourceApp,
    sourceAppDisplay = sourceAppDisplay,
    sender = sender,
    createdAt = createdAt,
    completedAt = completedAt,
    dueAt = dueAt,
    priority = TaskPriority.valueOf(priority),
    status = TaskStatus.valueOf(status),
    triggerKeywords = json.decodeFromString(ListSerializer(String.serializer()), triggerKeywordsJson),
    confidence = confidence,
    needsConfirmation = needsConfirmation,
    calendarEventId = calendarEventId,
    language = language,
)

fun Task.toEntity() = TaskEntity(
    id = id.toString(),
    text = text,
    rawSourceText = rawSourceText,
    sourceApp = sourceApp,
    sourceAppDisplay = sourceAppDisplay,
    sender = sender,
    createdAt = createdAt,
    completedAt = completedAt,
    dueAt = dueAt,
    priority = priority.name,
    status = status.name,
    triggerKeywordsJson = json.encodeToString(ListSerializer(String.serializer()), triggerKeywords),
    confidence = confidence,
    needsConfirmation = needsConfirmation,
    calendarEventId = calendarEventId,
    language = language,
)

fun NotificationEntity.toDomain() = NotificationRecord(
    id = id,
    packageName = packageName,
    sourceAppDisplay = sourceAppDisplay,
    sender = sender,
    title = title,
    body = body,
    conversationType = conversationType,
    rawText = rawText,
    receivedAtEpochMillis = receivedAtEpochMillis,
)

fun NotificationRecord.toEntity(decision: String = "NEW") = NotificationEntity(
    id = id,
    packageName = packageName,
    sourceAppDisplay = sourceAppDisplay,
    sender = sender,
    title = title,
    body = body,
    conversationType = conversationType,
    rawText = rawText,
    receivedAtEpochMillis = receivedAtEpochMillis,
    decision = decision,
)

fun ContactEntity.toDomain() = ContactProfile(
    id = id,
    displayName = displayName,
    trust = ContactTrust.valueOf(trust),
    learnedWeight = learnedWeight,
)

fun ContactProfile.toEntity() = ContactEntity(
    id = id,
    displayName = displayName,
    trust = trust.name,
    learnedWeight = learnedWeight,
)

fun KeywordRuleEntity.toDomain() = KeywordRule(
    id = id,
    phrase = phrase,
    category = category,
    weight = weight,
    languageHint = languageHint,
    enabled = enabled,
)

fun KeywordRule.toEntity() = KeywordRuleEntity(
    id = id,
    phrase = phrase,
    category = category,
    weight = weight,
    languageHint = languageHint,
    enabled = enabled,
)

fun LearningEvent.toEntity() = LearningEventEntity(
    id = id,
    taskId = taskId,
    notificationId = notificationId,
    eventType = eventType.name,
    payload = payload,
    createdAtEpochMillis = createdAtEpochMillis,
)

fun LearningEventEntity.toDomain() = LearningEvent(
    id = id,
    taskId = taskId,
    notificationId = notificationId,
    eventType = LearningEventType.valueOf(eventType),
    payload = payload,
    createdAtEpochMillis = createdAtEpochMillis,
)

fun AnalyticsSnapshotEntity.toDomain() = AnalyticsSnapshot(
    completedToday = completedToday,
    completedWeek = completedWeek,
    completedMonth = completedMonth,
    pendingBacklog = pendingBacklog,
    completionRate = completionRate,
    ignoredToday = ignoredToday,
    mostCommonSource = mostCommonSource,
)

fun AppSettingsEntity.toDomain() = AppSettings(
    onboardingCompleted = onboardingCompleted,
    reminderMode = com.rishabh.todo.codex.domain.model.ReminderMode.valueOf(reminderMode),
    reminderIntervalMinutes = reminderIntervalMinutes,
    emailReportHour = emailReportHour,
    dailyReportEnabled = dailyReportEnabled,
    biometricLockEnabled = biometricLockEnabled,
    scheduledExportEnabled = scheduledExportEnabled,
    scheduledExportPath = scheduledExportPath,
    scheduledExportFormat = scheduledExportFormat,
)

fun AppSettings.toEntity() = AppSettingsEntity(
    onboardingCompleted = onboardingCompleted,
    reminderMode = reminderMode.name,
    reminderIntervalMinutes = reminderIntervalMinutes,
    emailReportHour = emailReportHour,
    dailyReportEnabled = dailyReportEnabled,
    biometricLockEnabled = biometricLockEnabled,
    scheduledExportEnabled = scheduledExportEnabled,
    scheduledExportPath = scheduledExportPath,
    scheduledExportFormat = scheduledExportFormat,
)
