package com.rishabh.todo.codex.domain.usecase

import com.rishabh.todo.codex.domain.model.ExtractionResult
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.Task

class CreateTaskFromExtractionUseCase {
    operator fun invoke(
        notification: NotificationRecord,
        extraction: ExtractionResult,
        transcriptDerived: Boolean = false,
        ownerLabel: String? = null,
    ): Task {
        return Task(
            text = extraction.title, // Actionable task description
            rawSourceText = notification.rawText,
            sourceApp = notification.packageName,
            sourceAppDisplay = extraction.sourceAppDisplay,
            sender = extraction.sender ?: notification.sender,
            dueAt = extraction.dueAtEpochMillis,
            priority = extraction.priority,
            confidence = extraction.confidence,
            triggerKeywords = extraction.reason.matchedKeywords,
            needsConfirmation = extraction.decision == com.rishabh.todo.codex.domain.model.TaskCreationDecision.INBOX_REVIEW
        )
    }
}
