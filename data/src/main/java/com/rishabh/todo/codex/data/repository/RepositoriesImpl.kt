package com.rishabh.todo.codex.data.repository

import com.rishabh.todo.codex.data.local.AnalyticsDao
import com.rishabh.todo.codex.data.local.AnalyticsSnapshotEntity
import com.rishabh.todo.codex.data.local.ContactDao
import com.rishabh.todo.codex.data.local.SettingsDao
import com.rishabh.todo.codex.data.local.TaskDao
import com.rishabh.todo.codex.data.local.toDomain
import com.rishabh.todo.codex.data.local.toEntity
import com.rishabh.todo.codex.domain.model.AnalyticsSnapshot
import com.rishabh.todo.codex.domain.model.AppSettings
import com.rishabh.todo.codex.domain.model.ContactProfile
import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.repository.AnalyticsRepository
import com.rishabh.todo.codex.domain.repository.ContactPolicyRepository
import com.rishabh.todo.codex.domain.repository.NotificationRepository
import com.rishabh.todo.codex.domain.repository.SettingsRepository
import com.rishabh.todo.codex.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
) : TaskRepository {
    override fun observeTasks() = taskDao.observeTasks().map { it.map { entity -> entity.toDomain() } }

    override fun observePendingTasks() = taskDao.observePendingTasks().map { it.map { entity -> entity.toDomain() } }

    override suspend fun upsert(task: com.rishabh.todo.codex.domain.model.Task) { taskDao.upsert(task.toEntity()) }

    override suspend fun update(task: com.rishabh.todo.codex.domain.model.Task) = taskDao.update(task.toEntity())

    override suspend fun delete(taskId: java.util.UUID) = taskDao.delete(taskId.toString())

    override suspend fun complete(taskId: java.util.UUID) = taskDao.complete(taskId.toString())
}

class NotificationRepositoryImpl @Inject constructor(
    private val dao: com.rishabh.todo.codex.data.local.NotificationDao,
) : NotificationRepository {
    override fun observeInboxCandidates(): Flow<List<NotificationRecord>> =
        dao.observeInboxCandidates().map { rows -> rows.map { it.toDomain() } }

    override suspend fun save(record: NotificationRecord, decision: String): Long = dao.insert(record.toEntity(decision))

    override suspend fun updateDecision(notificationId: Long, decision: String) = dao.updateDecision(notificationId, decision)
}

class ContactPolicyRepositoryImpl @Inject constructor(
    private val dao: ContactDao,
) : ContactPolicyRepository {
    override fun observeContacts() = dao.observeContacts().map { it.map { entity -> entity.toDomain() } }
    override suspend fun upsert(contactProfile: ContactProfile) = dao.upsert(contactProfile.toEntity())
    override suspend fun getByName(name: String) = dao.getByName(name)?.toDomain()
}

class SettingsRepositoryImpl @Inject constructor(
    private val dao: SettingsDao,
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = dao.observeSettings().map {
        it?.toDomain() ?: AppSettings()
    }

    override suspend fun update(settings: AppSettings) = dao.upsert(settings.toEntity())
}

class AnalyticsRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val analyticsDao: AnalyticsDao,
    private val learningDao: com.rishabh.todo.codex.data.local.LearningDao,
) : AnalyticsRepository {
    override fun observeSnapshot(): Flow<AnalyticsSnapshot> = analyticsDao.observeSnapshot().map {
        it?.toDomain() ?: AnalyticsSnapshot(0, 0, 0, 0, 0f, 0, "None")
    }

    override suspend fun refresh() {
        val tasks = taskDao.getAll()
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(now, zone)
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val monthStart = today.withDayOfMonth(1)
        val completed = tasks.filter { it.status == "COMPLETED" }
        val total = tasks.size.coerceAtLeast(1)
        fun sameOrAfter(epochMillis: Long, date: LocalDate): Boolean =
            Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate() >= date

        val ignoredToday = learningDao.getRejectedPhrases().size
        val source = tasks.groupingBy { it.sourceAppDisplay }.eachCount().maxByOrNull { it.value }?.key ?: "None"

        analyticsDao.upsert(
            AnalyticsSnapshotEntity(
                completedToday = completed.count { sameOrAfter(it.createdAt, today) },
                completedWeek = completed.count { sameOrAfter(it.createdAt, weekStart) },
                completedMonth = completed.count { sameOrAfter(it.createdAt, monthStart) },
                pendingBacklog = tasks.count { it.status == "PENDING" },
                completionRate = completed.size.toFloat() / total.toFloat(),
                ignoredToday = ignoredToday,
                mostCommonSource = source,
            ),
        )
    }
}
