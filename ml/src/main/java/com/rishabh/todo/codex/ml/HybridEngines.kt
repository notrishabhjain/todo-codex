package com.rishabh.todo.codex.ml

import com.rishabh.todo.codex.domain.engine.ExtractionEngine
import com.rishabh.todo.codex.domain.engine.LearningEngine
import com.rishabh.todo.codex.domain.engine.ReminderScheduler
import com.rishabh.todo.codex.domain.engine.TranscriptExtractionEngine
import com.rishabh.todo.codex.domain.model.ContactTrust
import com.rishabh.todo.codex.domain.model.ExtractionReason
import com.rishabh.todo.codex.domain.model.ExtractionResult
import com.rishabh.todo.codex.domain.model.LearningEvent
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.ReminderPolicy
import com.rishabh.todo.codex.domain.model.ReminderMode
import com.rishabh.todo.codex.domain.model.SourceType
import com.rishabh.todo.codex.domain.model.Task
import com.rishabh.todo.codex.domain.model.TaskCreationDecision
import com.rishabh.todo.codex.domain.model.TaskPriority
import com.rishabh.todo.codex.domain.model.TranscriptCandidateTask
import com.rishabh.todo.codex.domain.repository.ContactPolicyRepository
import com.rishabh.todo.codex.domain.repository.LearningRepository
import com.rishabh.todo.codex.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

class HybridExtractionEngine @Inject constructor(
    private val contactPolicyRepository: ContactPolicyRepository,
) : ExtractionEngine {
    override suspend fun extract(notification: NotificationRecord): ExtractionResult {
        val text = notification.rawText.lowercase()
        val matchedActions = actionKeywords.filter { text.contains(it) }
        val matchedUrgency = urgencyKeywords.filter { text.contains(it) }
        val due = DueDateParser.parse(text)
        val contact = notification.sender?.let { contactPolicyRepository.getByName(it) }
        val senderWeight = when (contact?.trust ?: ContactTrust.NORMAL) {
            ContactTrust.VIP -> 1f
            ContactTrust.HIGH_PRIORITY -> 0.75f
            ContactTrust.NORMAL -> 0.5f
            ContactTrust.IGNORE -> 0f
        }
        val modelScore = score(text, matchedActions, matchedUrgency, due != null, senderWeight)
        val actionable = matchedActions.isNotEmpty() || modelScore >= 0.55f
        val priority = when {
            matchedUrgency.isNotEmpty() || senderWeight >= 0.75f -> TaskPriority.HIGH
            notification.sourceType == SourceType.CALENDAR -> TaskPriority.CRITICAL
            else -> TaskPriority.MEDIUM
        }
        val decision = when {
            !actionable -> TaskCreationDecision.IGNORE
            notification.sourceType == SourceType.CALENDAR -> TaskCreationDecision.AUTO_CREATE
            else -> TaskCreationDecision.INBOX_REVIEW
        }
        return ExtractionResult(
            actionable = actionable,
            title = summarizeTitle(notification.rawText, matchedActions),
            description = notification.rawText,
            priority = priority,
            dueAtEpochMillis = due,
            decision = decision,
            confidence = modelScore,
            sender = notification.sender,
            sourceType = notification.sourceType,
            reason = ExtractionReason(
                matchedKeywords = matchedActions + matchedUrgency,
                inferredDatePhrase = timeKeywords.firstOrNull { text.contains(it) },
                modelScore = modelScore,
                senderWeight = senderWeight,
                threshold = 0.55f,
            ),
        )
    }

    private fun score(
        text: String,
        actions: List<String>,
        urgency: List<String>,
        hasDate: Boolean,
        senderWeight: Float,
    ): Float {
        val base = (actions.size * 0.22f) + (urgency.size * 0.18f) + if (hasDate) 0.18f else 0f
        val lengthBonus = if (text.length in 8..140) 0.08f else 0f
        return (base + senderWeight * 0.25f + lengthBonus).coerceIn(0f, 1f)
    }

    private fun summarizeTitle(raw: String, actions: List<String>): String {
        val clean = raw.replace(Regex("\\s+"), " ").trim()
        val action = actions.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Follow up"
        val sliced = clean.split(" ").take(6).joinToString(" ")
        return when {
            clean.length <= 42 -> clean.replaceFirstChar { it.uppercase() }
            else -> "$action: $sliced"
        }
    }
}

object DueDateParser {
    fun parse(text: String, now: Instant = Instant.now()): Long? {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(now, zone)
        return when {
            text.contains("aaj") || text.contains("today") -> today.atTime(LocalTime.of(18, 0)).atZone(zone).toInstant().toEpochMilli()
            text.contains("kal") || text.contains("tomorrow") -> today.plusDays(1).atTime(LocalTime.of(18, 0)).atZone(zone).toInstant().toEpochMilli()
            text.contains("shaam tak") || text.contains("by evening") || text.contains("eod") -> today.atTime(LocalTime.of(19, 0)).atZone(zone).toInstant().toEpochMilli()
            text.contains("raat tak") || text.contains("tonight") -> today.atTime(LocalTime.of(22, 0)).atZone(zone).toInstant().toEpochMilli()
            text.contains("agle hafte") || text.contains("next week") -> today.plusWeeks(1).atTime(LocalTime.of(10, 0)).atZone(zone).toInstant().toEpochMilli()
            else -> findWeekday(text, today, zone)
        }
    }

    private fun findWeekday(text: String, today: LocalDate, zone: ZoneId): Long? {
        val days = mapOf(
            "monday" to DayOfWeek.MONDAY,
            "tuesday" to DayOfWeek.TUESDAY,
            "wednesday" to DayOfWeek.WEDNESDAY,
            "thursday" to DayOfWeek.THURSDAY,
            "friday" to DayOfWeek.FRIDAY,
            "saturday" to DayOfWeek.SATURDAY,
            "sunday" to DayOfWeek.SUNDAY,
        )
        val match = days.entries.firstOrNull { text.contains(it.key) }?.value ?: return null
        return today.with(TemporalAdjusters.nextOrSame(match)).atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
    }
}

class TranscriptExtractionEngineImpl @Inject constructor() : TranscriptExtractionEngine {
    override suspend fun extract(transcript: String): List<TranscriptCandidateTask> {
        return transcript.split(".", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { sentence ->
                val lower = sentence.lowercase()
                val actions = actionKeywords.filter { lower.contains(it) }
                if (actions.isEmpty()) return@mapNotNull null
                val owner = sentence.substringBefore(" will", "").takeIf { sentence.contains(" will") && it.isNotBlank() }
                TranscriptCandidateTask(
                    owner = owner,
                    title = sentence.split(" ").take(6).joinToString(" "),
                    description = sentence,
                    dueAtEpochMillis = DueDateParser.parse(lower),
                    priority = if (urgencyKeywords.any { lower.contains(it) }) TaskPriority.HIGH else TaskPriority.MEDIUM,
                    confidence = 0.72f,
                )
            }
    }
}

class LocalLearningEngine @Inject constructor(
    private val learningRepository: LearningRepository,
) : LearningEngine {
    override suspend fun record(event: LearningEvent) {
        learningRepository.record(event)
    }

    override suspend fun recalculateProfiles() {
        learningRepository.getAcceptedPhrases()
        learningRepository.getRejectedPhrases()
    }
}

class ReminderSchedulerFacade @Inject constructor(
    private val scheduler: ReminderScheduler,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun scheduleFor(tasks: List<Task>) {
        val settings = settingsRepository.observeSettings().first()
        scheduler.schedule(
            ReminderPolicy(
                mode = settings.reminderMode,
                intervalMinutes = settings.reminderIntervalMinutes,
            ),
            tasks,
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object MlModule {
    @Provides
    @Singleton
    fun provideExtractionEngine(contactPolicyRepository: ContactPolicyRepository): ExtractionEngine {
        return HybridExtractionEngine(contactPolicyRepository)
    }

    @Provides
    @Singleton
    fun provideTranscriptEngine(): TranscriptExtractionEngine = TranscriptExtractionEngineImpl()

    @Provides
    @Singleton
    fun provideLearningEngine(learningRepository: LearningRepository): LearningEngine {
        return LocalLearningEngine(learningRepository)
    }
}
