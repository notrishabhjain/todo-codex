package com.rishabh.todo.codex

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rishabh.todo.codex.data.integration.DailyReportEmailBuilder
import com.rishabh.todo.codex.domain.engine.ExtractionEngine
import com.rishabh.todo.codex.domain.engine.TranscriptExtractionEngine
import com.rishabh.todo.codex.domain.model.AnalyticsSnapshot
import com.rishabh.todo.codex.domain.model.AppSettings
import com.rishabh.todo.codex.domain.model.ExtractionResult
import com.rishabh.todo.codex.domain.model.ExtractionReason
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.SourceType
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.domain.model.TaskCreationDecision
import com.rishabh.todo.codex.domain.model.TaskPriority
import com.rishabh.todo.codex.domain.model.TranscriptCandidateTask
import com.rishabh.todo.codex.domain.repository.AnalyticsRepository
import com.rishabh.todo.codex.domain.repository.ExportRepository
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
    private val analyticsRepository: AnalyticsRepository,
    private val settingsRepository: SettingsRepository,
    private val exportRepository: ExportRepository,
    private val extractionEngine: ExtractionEngine,
    private val transcriptExtractionEngine: TranscriptExtractionEngine,
    private val dailyReportEmailBuilder: DailyReportEmailBuilder,
) : ViewModel() {
    private val transcript = MutableStateFlow("")
    private val transcriptCandidates = MutableStateFlow<List<TranscriptCandidateTask>>(emptyList())
    private val createTask = CreateTaskFromExtractionUseCase()

    private data class BaseStateBundle(
        val tasks: List<Task>,
        val inbox: List<NotificationRecord>,
        val analytics: AnalyticsSnapshot,
        val settings: AppSettings,
    )

    val state = combine(
        combine(
            combine(
                combine(
                    taskRepository.observeTasks(),
                    notificationRepository.observeInboxCandidates(),
                ) { tasks: List<Task>, inbox: List<NotificationRecord> ->
                    tasks to inbox
                },
                analyticsRepository.observeSnapshot(),
            ) { taskInboxPair: Pair<List<Task>, List<NotificationRecord>>, analytics: AnalyticsSnapshot ->
                Triple(taskInboxPair.first, taskInboxPair.second, analytics)
            },
            settingsRepository.observeSettings(),
        ) { taskInboxAnalytics: Triple<List<Task>, List<NotificationRecord>, AnalyticsSnapshot>, settings: AppSettings ->
            BaseStateBundle(
                tasks = taskInboxAnalytics.first,
                inbox = taskInboxAnalytics.second,
                analytics = taskInboxAnalytics.third,
                settings = settings,
            )
        },
        transcript,
        transcriptCandidates,
    ) { bundle: BaseStateBundle, transcriptText: String, candidates: List<TranscriptCandidateTask> ->
        MainUiState(
            tasks = bundle.tasks,
            inbox = bundle.inbox,
            analytics = bundle.analytics,
            settings = bundle.settings,
            transcript = transcriptText,
            transcriptCandidates = candidates,
            motivationLine = buildMotivationLine(bundle.tasks, bundle.analytics),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    init {
        viewModelScope.launch { analyticsRepository.refresh() }
    }

    fun approveNotification(record: NotificationRecord) {
        viewModelScope.launch {
            val extraction = extractionEngine.extract(record)
            taskRepository.upsert(createTask(record, extraction))
            notificationRepository.updateDecision(record.id, "APPROVED")
            analyticsRepository.refresh()
        }
    }

    fun ignoreNotification(record: NotificationRecord) {
        viewModelScope.launch {
            notificationRepository.updateDecision(record.id, "IGNORED")
            analyticsRepository.refresh()
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
                    taskRepository.upsert(createTask(pseudoNotification, extraction, transcriptDerived = true, ownerLabel = candidate.owner))
                }
            analyticsRepository.refresh()
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            exportRepository.exportJson("offline-task-export.json")
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            exportRepository.exportCsv("offline-task-export.csv")
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
}
