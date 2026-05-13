package com.rishabh.todo.codex

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rishabh.todo.codex.data.background.AutomationScheduler
import com.rishabh.todo.codex.data.bootstrap.KeywordRuleSeeder
import com.rishabh.todo.codex.data.integration.CalendarIntegrationManager
import com.rishabh.todo.codex.data.integration.DailyReportEmailBuilder
import com.rishabh.todo.codex.domain.engine.ExtractionEngine
import com.rishabh.todo.codex.domain.engine.LearningEngine
import com.rishabh.todo.codex.domain.engine.ReminderScheduler
import com.rishabh.todo.codex.domain.engine.TranscriptExtractionEngine
import com.rishabh.todo.codex.domain.model.AnalyticsSnapshot
import com.rishabh.todo.codex.domain.model.AppSettings
import com.rishabh.todo.codex.domain.model.ContactProfile
import com.rishabh.todo.codex.domain.model.ContactTrust
import com.rishabh.todo.codex.domain.model.ExtractionReason
import com.rishabh.todo.codex.domain.model.ExtractionResult
import com.rishabh.todo.codex.domain.model.KeywordRule
import com.rishabh.todo.codex.domain.model.LearningEvent
import com.rishabh.todo.codex.domain.model.LearningEventType
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.ReminderMode
import com.rishabh.todo.codex.domain.model.ReminderPolicy
import com.rishabh.todo.codex.domain.model.SourceType
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.domain.model.TaskCreationDecision
import com.rishabh.todo.codex.domain.model.TaskPriority
import com.rishabh.todo.codex.domain.model.TaskStatus
import com.rishabh.todo.codex.domain.model.TranscriptCandidateTask
import com.rishabh.todo.codex.domain.repository.AnalyticsRepository
import com.rishabh.todo.codex.domain.repository.ContactPolicyRepository
import com.rishabh.todo.codex.domain.repository.ExportRepository
import com.rishabh.todo.codex.domain.repository.KeywordRuleRepository
import com.rishabh.todo.codex.domain.repository.NotificationRepository
import com.rishabh.todo.codex.domain.repository.SettingsRepository
import com.rishabh.todo.codex.domain.repository.TaskRepository
import com.rishabh.todo.codex.domain.usecase.CreateTaskFromExtractionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val tasks: List<Task> = emptyList(),
    val inbox: List<NotificationRecord> = emptyList(),
    val contacts: List<ContactProfile> = emptyList(),
    val keywordRules: List<KeywordRule> = emptyList(),
    val analytics: AnalyticsSnapshot = AnalyticsSnapshot(0, 0, 0, 0, 0f, 0, "None"),
    val settings: AppSettings = AppSettings(),
    val transcript: String = "",
    val transcriptCandidates: List<TranscriptCandidateTask> = emptyList(),
    val motivationLine: String = "Keep the important things visible and moving.",
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val notificationRepository: NotificationRepository,
    private val contactPolicyRepository: ContactPolicyRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val settingsRepository: SettingsRepository,
    private val exportRepository: ExportRepository,
    private val keywordRuleRepository: KeywordRuleRepository,
    private val extractionEngine: ExtractionEngine,
    private val transcriptExtractionEngine: TranscriptExtractionEngine,
    private val learningEngine: LearningEngine,
    private val reminderScheduler: ReminderScheduler,
    private val automationScheduler: AutomationScheduler,
    private val calendarIntegrationManager: CalendarIntegrationManager,
    private val keywordRuleSeeder: KeywordRuleSeeder,
    private val dailyReportEmailBuilder: DailyReportEmailBuilder,
) : ViewModel() {
    private val transcript = MutableStateFlow("")
    private val transcriptCandidates = MutableStateFlow<List<TranscriptCandidateTask>>(emptyList())
    private val createTask = CreateTaskFromExtractionUseCase()

    private data class BaseStateBundle(
        val tasks: List<Task>,
        val inbox: List<NotificationRecord>,
        val contacts: List<ContactProfile>,
        val keywordRules: List<KeywordRule>,
        val analytics: AnalyticsSnapshot,
        val settings: AppSettings,
    )

    private val baseState = combine(
        combine(
            combine(
                combine(
                    combine(
                        taskRepository.observeTasks(),
                        notificationRepository.observeInboxCandidates(),
                    ) { tasks: List<Task>, inbox: List<NotificationRecord> ->
                        Pair(tasks, inbox)
                    },
                    contactPolicyRepository.observeContacts(),
                ) { taskInbox: Pair<List<Task>, List<NotificationRecord>>, contacts: List<ContactProfile> ->
                    Triple(taskInbox.first, taskInbox.second, contacts)
                },
                keywordRuleRepository.observeRules(),
            ) { taskInboxContacts: Triple<List<Task>, List<NotificationRecord>, List<ContactProfile>>, rules: List<KeywordRule> ->
                Quadruple(taskInboxContacts.first, taskInboxContacts.second, taskInboxContacts.third, rules)
            },
            analyticsRepository.observeSnapshot(),
        ) { taskInboxContactsRules: Quadruple<List<Task>, List<NotificationRecord>, List<ContactProfile>, List<KeywordRule>>, analytics: AnalyticsSnapshot ->
            Quintuple(
                taskInboxContactsRules.first,
                taskInboxContactsRules.second,
                taskInboxContactsRules.third,
                taskInboxContactsRules.fourth,
                analytics,
            )
        },
        settingsRepository.observeSettings(),
    ) { bundle: Quintuple<List<Task>, List<NotificationRecord>, List<ContactProfile>, List<KeywordRule>, AnalyticsSnapshot>, settings: AppSettings ->
        BaseStateBundle(
            tasks = bundle.first,
            inbox = bundle.second,
            contacts = bundle.third,
            keywordRules = bundle.fourth,
            analytics = bundle.fifth,
            settings = settings,
        )
    }

    val state = combine(
        baseState,
        transcript,
        transcriptCandidates,
    ) { bundle: BaseStateBundle, transcriptText: String, candidates: List<TranscriptCandidateTask> ->
        MainUiState(
            tasks = bundle.tasks,
            inbox = bundle.inbox,
            contacts = bundle.contacts,
            keywordRules = bundle.keywordRules,
            analytics = bundle.analytics,
            settings = bundle.settings,
            transcript = transcriptText,
            transcriptCandidates = candidates,
            motivationLine = buildMotivationLine(bundle.tasks, bundle.analytics),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    init {
        viewModelScope.launch {
            keywordRuleSeeder.seedIfEmpty()
            analyticsRepository.refresh()
            scheduleAutomation()
        }
    }

    fun approveNotification(record: NotificationRecord) {
        viewModelScope.launch {
            val extraction = extractionEngine.extract(record)
            taskRepository.upsert(createTask(record, extraction))
            notificationRepository.updateDecision(record.id, "APPROVED")
            recordLearning(record.id, null, LearningEventType.APPROVED, record.rawText)
            analyticsRepository.refresh()
            rescheduleReminders()
        }
    }

    fun ignoreNotification(record: NotificationRecord) {
        viewModelScope.launch {
            notificationRepository.updateDecision(record.id, "IGNORED")
            recordLearning(record.id, null, LearningEventType.IGNORED, record.rawText)
            analyticsRepository.refresh()
        }
    }

    fun trustContactFromNotification(record: NotificationRecord, trust: ContactTrust) {
        val sender = record.sender ?: return
        viewModelScope.launch {
            val existing = contactPolicyRepository.getByName(sender)
            contactPolicyRepository.upsert(
                ContactProfile(
                    id = existing?.id ?: 0L,
                    displayName = sender,
                    trust = trust,
                    learnedWeight = existing?.learnedWeight ?: 0.8f,
                ),
            )
            if (trust == ContactTrust.IGNORE) {
                notificationRepository.updateDecision(record.id, "IGNORED")
            } else {
                val extraction = extractionEngine.extract(record)
                taskRepository.upsert(createTask(record, extraction))
                notificationRepository.updateDecision(record.id, "APPROVED")
                recordLearning(record.id, null, LearningEventType.APPROVED, record.rawText)
                analyticsRepository.refresh()
                rescheduleReminders()
            }
        }
    }

    fun updateTranscript(value: String) {
        transcript.value = value
    }

    fun extractTranscript() {
        viewModelScope.launch {
            transcriptCandidates.value = transcriptExtractionEngine.extract(transcript.value)
        }
    }

    fun importTranscriptCandidates(myName: String? = null, includeAllOwners: Boolean = true) {
        viewModelScope.launch {
            transcriptCandidates.value
                .filter { includeAllOwners || it.owner == null || it.owner.equals(myName, ignoreCase = true) }
                .forEach { candidate ->
                    val pseudoNotification = NotificationRecord(
                        packageName = "transcript",
                        sourceType = SourceType.GENERIC,
                        sender = candidate.owner,
                        title = candidate.title,
                        body = candidate.description,
                        conversationType = null,
                        rawText = candidate.description,
                        receivedAtEpochMillis = System.currentTimeMillis(),
                    )
                    val extraction = ExtractionResult(
                        actionable = true,
                        title = candidate.title,
                        description = candidate.description,
                        priority = candidate.priority,
                        dueAtEpochMillis = candidate.dueAtEpochMillis,
                        decision = TaskCreationDecision.AUTO_CREATE,
                        confidence = candidate.confidence,
                        sender = candidate.owner,
                        sourceType = SourceType.GENERIC,
                        reason = ExtractionReason(modelScore = candidate.confidence),
                    )
                    taskRepository.upsert(
                        createTask(
                            pseudoNotification,
                            extraction,
                            transcriptDerived = true,
                            ownerLabel = candidate.owner,
                        ),
                    )
                }
            analyticsRepository.refresh()
            rescheduleReminders()
        }
    }

    fun exportJson() {
        viewModelScope.launch { exportRepository.exportJson("offline-task-export.json") }
    }

    fun exportCsv() {
        viewModelScope.launch { exportRepository.exportCsv("offline-task-export.csv") }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.complete(task.id)
            recordLearning(null, task.id, LearningEventType.COMPLETED, task.title)
            analyticsRepository.refresh()
            rescheduleReminders()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task.id)
            analyticsRepository.refresh()
            rescheduleReminders()
        }
    }

    fun archiveTask(task: Task) {
        viewModelScope.launch {
            taskRepository.update(task.copy(status = TaskStatus.ARCHIVED))
            analyticsRepository.refresh()
            rescheduleReminders()
        }
    }

    fun raiseTaskPriority(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(
                priority = when (task.priority) {
                    TaskPriority.LOW -> TaskPriority.MEDIUM
                    TaskPriority.MEDIUM -> TaskPriority.HIGH
                    TaskPriority.HIGH -> TaskPriority.CRITICAL
                    TaskPriority.CRITICAL -> TaskPriority.CRITICAL
                },
            )
            taskRepository.update(updated)
            recordLearning(null, task.id, LearningEventType.MANUALLY_EDITED, updated.title)
            analyticsRepository.refresh()
            rescheduleReminders()
        }
    }

    fun addTaskToCalendar(task: Task) {
        viewModelScope.launch {
            val eventId = calendarIntegrationManager.createEvent(task) ?: return@launch
            taskRepository.update(task.copy(linkedCalendarEventId = eventId))
        }
    }

    fun updateReminderMode(mode: ReminderMode) {
        viewModelScope.launch {
            settingsRepository.update(state.value.settings.copy(reminderMode = mode))
            scheduleAutomation()
            rescheduleReminders()
        }
    }

    fun updateReminderInterval(intervalMinutes: Long) {
        viewModelScope.launch {
            settingsRepository.update(state.value.settings.copy(reminderIntervalMinutes = intervalMinutes))
            scheduleAutomation()
            rescheduleReminders()
        }
    }

    fun toggleDailyReport(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.update(state.value.settings.copy(dailyReportEnabled = enabled))
            scheduleAutomation()
        }
    }

    fun toggleScheduledExport(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.update(
                state.value.settings.copy(
                    scheduledExportEnabled = enabled,
                    scheduledExportPath = state.value.settings.scheduledExportPath
                        ?: "scheduled-export.${state.value.settings.scheduledExportFormat}",
                ),
            )
            scheduleAutomation()
        }
    }

    fun updateExportFormat(format: String) {
        viewModelScope.launch {
            settingsRepository.update(
                state.value.settings.copy(
                    scheduledExportFormat = format,
                    scheduledExportPath = "scheduled-export.$format",
                ),
            )
            scheduleAutomation()
        }
    }

    fun setContactTrust(contactProfile: ContactProfile, trust: ContactTrust) {
        viewModelScope.launch {
            contactPolicyRepository.upsert(contactProfile.copy(trust = trust))
        }
    }

    fun toggleKeywordRule(rule: KeywordRule) {
        viewModelScope.launch {
            keywordRuleRepository.upsert(rule.copy(enabled = !rule.enabled))
        }
    }

    fun buildDailyEmail(): Intent? {
        val snapshot = state.value.analytics
        val summary = """
            Completed today: ${snapshot.completedToday}
            Pending backlog: ${snapshot.pendingBacklog}
            Completion rate: ${(snapshot.completionRate * 100).toInt()}%
            Most common source: ${snapshot.mostCommonSource}
        """.trimIndent()
        return dailyReportEmailBuilder.buildIntent(summary)
    }

    private fun buildMotivationLine(tasks: List<Task>, analytics: AnalyticsSnapshot): String {
        return when {
            analytics.pendingBacklog == 0 -> "Inbox clear. Protect the momentum."
            tasks.count { it.priority == TaskPriority.HIGH || it.priority == TaskPriority.CRITICAL } >= 3 ->
                "High-pressure queue detected. Finish the top commitment first."
            else -> "${analytics.pendingBacklog} commitments still open. Keep moving."
        }
    }

    private suspend fun rescheduleReminders() {
        reminderScheduler.schedule(
            ReminderPolicy(
                mode = state.value.settings.reminderMode,
                intervalMinutes = state.value.settings.reminderIntervalMinutes,
            ),
            state.value.tasks.filter { it.status == TaskStatus.PENDING },
        )
    }

    private suspend fun scheduleAutomation() {
        automationScheduler.schedule(state.value.settings)
    }

    private suspend fun recordLearning(
        notificationId: Long?,
        taskId: Long?,
        type: LearningEventType,
        payload: String,
    ) {
        learningEngine.record(
            LearningEvent(
                taskId = taskId,
                notificationId = notificationId,
                eventType = type,
                payload = payload,
            ),
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)
