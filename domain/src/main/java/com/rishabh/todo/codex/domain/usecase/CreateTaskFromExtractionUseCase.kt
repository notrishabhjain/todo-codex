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
            title = extraction.title,
            description = extraction.description,
            sourceType = extraction.sourceType,
            sender = extraction.sender ?: notification.sender,
            dueAtEpochMillis = extraction.dueAtEpochMillis,
            priority = extraction.priority,
            originalNotificationText = notification.rawText,
            transcriptDerived = transcriptDerived,
            ownerLabel = ownerLabel,
            confidence = extraction.confidence,
        )
    }
}
