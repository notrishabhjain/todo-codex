package com.rishabh.todo.codex.data.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.rishabh.todo.codex.domain.engine.ExtractionEngine
import com.rishabh.todo.codex.domain.model.ContactTrust
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.SourceType
import com.rishabh.todo.codex.domain.repository.ContactPolicyRepository
import com.rishabh.todo.codex.domain.repository.NotificationRepository
import com.rishabh.todo.codex.domain.repository.TaskRepository
import com.rishabh.todo.codex.domain.usecase.CreateTaskFromExtractionUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationCaptureService : NotificationListenerService() {
    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var contactPolicyRepository: ContactPolicyRepository
    @Inject lateinit var extractionEngine: ExtractionEngine
    @Inject lateinit var taskRepository: TaskRepository

    private val createTask = CreateTaskFromExtractionUseCase()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val body = extras.getCharSequence("android.text")?.toString()
        val sender = extras.getString("android.conversationTitle") ?: title
        val sourceType = normalizeSource(sbn.packageName)
        val conversationType = if (extras.getCharSequenceArray("android.messages")?.isNotEmpty() == true) "GROUP_OR_THREAD" else "SINGLE"
        val rawText = listOfNotNull(title, body).joinToString(" ").trim()
        if (rawText.isBlank()) return

        scope.launch {
            val record = NotificationRecord(
                packageName = sbn.packageName,
                sourceType = sourceType,
                sender = sender,
                title = title,
                body = body,
                conversationType = conversationType,
                rawText = rawText,
                receivedAtEpochMillis = sbn.postTime,
            )
            val extraction = extractionEngine.extract(record)
            val contactTrust = sender?.let { contactPolicyRepository.getByName(it)?.trust } ?: ContactTrust.NORMAL
            val decision = when {
                !extraction.actionable -> "IGNORE"
                sourceType == SourceType.CALENDAR -> "AUTO_CREATE"
                sourceType == SourceType.WHATSAPP && contactTrust == ContactTrust.VIP -> "AUTO_CREATE"
                contactTrust == ContactTrust.IGNORE -> "IGNORE"
                else -> extraction.decision.name
            }
            val notificationId = notificationRepository.save(record.copy(id = 0L))
            notificationRepository.updateDecision(notificationId, decision)
            if (decision == "AUTO_CREATE") {
                taskRepository.upsert(createTask(record, extraction))
            }
        }
    }

    private fun normalizeSource(packageName: String): SourceType = when {
        packageName.contains("whatsapp", ignoreCase = true) -> SourceType.WHATSAPP
        packageName.contains("gmail", ignoreCase = true) -> SourceType.GMAIL
        packageName.contains("telegram", ignoreCase = true) -> SourceType.TELEGRAM
        packageName.contains("slack", ignoreCase = true) -> SourceType.SLACK
        packageName.contains("sms", ignoreCase = true) || packageName.contains("messaging", ignoreCase = true) -> SourceType.SMS
        packageName.contains("calendar", ignoreCase = true) -> SourceType.CALENDAR
        else -> SourceType.GENERIC
    }
}
