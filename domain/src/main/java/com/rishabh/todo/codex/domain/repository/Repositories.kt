package com.rishabh.todo.codex.domain.repository

import com.rishabh.todo.codex.domain.model.AnalyticsSnapshot
import com.rishabh.todo.codex.domain.model.AppSettings
import com.rishabh.todo.codex.domain.model.ContactProfile
import com.rishabh.todo.codex.domain.model.KeywordRule
import com.rishabh.todo.codex.domain.model.LearningEvent
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observePendingTasks(): Flow<List<Task>>
    suspend fun upsert(task: Task)
    suspend fun update(task: Task)
    suspend fun delete(taskId: java.util.UUID)
    suspend fun complete(taskId: java.util.UUID)
}

interface NotificationRepository {
    fun observeInboxCandidates(): Flow<List<NotificationRecord>>
    suspend fun save(record: NotificationRecord, decision: String = "NEW"): Long
    suspend fun updateDecision(notificationId: Long, decision: String)
}

interface ContactPolicyRepository {
    fun observeContacts(): Flow<List<ContactProfile>>
    suspend fun upsert(contactProfile: ContactProfile)
    suspend fun getByName(name: String): ContactProfile?
}

interface AnalyticsRepository {
    fun observeSnapshot(): Flow<AnalyticsSnapshot>
    suspend fun refresh()
}

interface ExportRepository {
    suspend fun exportJson(destinationPath: String): Result<String>
    suspend fun exportCsv(destinationPath: String): Result<String>
    suspend fun importJson(sourcePath: String): Result<Int>
}

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun update(settings: AppSettings)
}

interface LearningRepository {
    suspend fun record(event: LearningEvent)
    suspend fun getAcceptedPhrases(): List<String>
    suspend fun getRejectedPhrases(): List<String>
}

interface KeywordRuleRepository {
    fun observeRules(): Flow<List<KeywordRule>>
    suspend fun getEnabledRules(): List<KeywordRule>
    suspend fun upsert(rule: KeywordRule)
    suspend fun count(): Int
    suspend fun delete(ruleId: Long)
}
