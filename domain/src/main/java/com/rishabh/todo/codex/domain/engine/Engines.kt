package com.rishabh.todo.codex.domain.engine

import com.rishabh.todo.codex.domain.model.ExtractionResult
import com.rishabh.todo.codex.domain.model.LearningEvent
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.ReminderPolicy
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.domain.model.TranscriptCandidateTask

interface ExtractionEngine {
    suspend fun extract(notification: NotificationRecord): ExtractionResult
}

interface TranscriptExtractionEngine {
    suspend fun extract(transcript: String): List<TranscriptCandidateTask>
}

interface ReminderScheduler {
    suspend fun schedule(policy: ReminderPolicy, tasks: List<Task>)
}

interface LearningEngine {
    suspend fun record(event: LearningEvent)
    suspend fun recalculateProfiles()
}
