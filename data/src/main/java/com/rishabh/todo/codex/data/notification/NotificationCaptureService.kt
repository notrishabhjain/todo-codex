package com.rishabh.todo.codex.data.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.rishabh.todo.codex.domain.engine.ExtractionEngine
import com.rishabh.todo.codex.domain.model.ContactTrust
import com.rishabh.todo.codex.domain.model.NotificationRecord

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
        val sourceAppDisplay = normalizeSourceAppDisplay(sbn.packageName)
        val conversationType = if (extras.getCharSequenceArray("android.messages")?.isNotEmpty() == true) "GROUP_OR_THREAD" else "SINGLE"
        val rawText = listOfNotNull(title, body).joinToString(" ").trim()
        if (rawText.isBlank()) return

        scope.launch {
            val record = NotificationRecord(
                packageName = sbn.packageName,
                sourceAppDisplay = sourceAppDisplay,
                sender = sender,
                title = title,
                body = body,
                conversationType = conversationType,
                rawText = rawText,
                receivedAtEpochMillis = sbn.postTime,
            )
            val extraction = extractionEngine.extract(record)
            val contactTrust = sender?.let { contactPolicyRepository.getByName(it)?.trust } ?: ContactTrust.NORMAL

            val isVip = contactTrust == ContactTrust.VIP
            val decision = when {
                contactTrust == ContactTrust.IGNORE -> "IGNORE"
                !extraction.actionable && !isVip -> "IGNORE"
                sourceAppDisplay == "Calendar" -> "AUTO_CREATE"
                isVip -> "AUTO_CREATE"
                else -> extraction.decision.name // AUTO_CREATE or INBOX_REVIEW
            }

            if (decision == "IGNORE") return@launch

            // Save with the resolved decision so the inbox query picks up INBOX_REVIEW items immediately
            notificationRepository.save(record.copy(id = 0L), decision)

            if (decision == "AUTO_CREATE") {
                val task = createTask(record, extraction)
                val finalTask = if (isVip)
                    task.copy(priority = com.rishabh.todo.codex.domain.model.TaskPriority.URGENT, needsConfirmation = false)
                else
                    task.copy(needsConfirmation = false)
                taskRepository.upsert(finalTask)
            }
            // INBOX_REVIEW: stays in inbox only — task created when user approves via MainViewModel.approveNotification()
        }
    }

    private fun normalizeSourceAppDisplay(packageName: String): String = when {
        packageName.contains("whatsapp", ignoreCase = true) -> "WhatsApp"
        packageName.contains("gmail", ignoreCase = true) -> "Gmail"
        packageName.contains("telegram", ignoreCase = true) -> "Telegram"
        packageName.contains("slack", ignoreCase = true) -> "Slack"
        packageName.contains("sms", ignoreCase = true) || packageName.contains("messaging", ignoreCase = true) -> "SMS"
        packageName.contains("calendar", ignoreCase = true) -> "Calendar"
        else -> packageName.substringAfterLast('.')
    }


}
