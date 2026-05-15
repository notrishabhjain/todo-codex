package com.rishabh.todo.codex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY dueAt IS NULL, dueAt ASC")
    fun observePendingTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun delete(taskId: String)

    @Query("UPDATE tasks SET status = 'COMPLETED' WHERE id = :taskId")
    suspend fun complete(taskId: String)

    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskEntity>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE decision = 'INBOX_REVIEW' ORDER BY receivedAtEpochMillis DESC")
    fun observeInboxCandidates(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationEntity): Long

    @Query("UPDATE notifications SET decision = :decision WHERE id = :notificationId")
    suspend fun updateDecision(notificationId: Long, decision: String)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY learnedWeight DESC")
    fun observeContacts(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ContactEntity)

    @Query("SELECT * FROM contacts WHERE displayName = :name LIMIT 1")
    suspend fun getByName(name: String): ContactEntity?
}

@Dao
interface LearningDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LearningEventEntity)

    @Query("SELECT payload FROM learning_events WHERE eventType = 'APPROVED'")
    suspend fun getAcceptedPhrases(): List<String>

    @Query("SELECT payload FROM learning_events WHERE eventType IN ('REJECTED', 'IGNORED')")
    suspend fun getRejectedPhrases(): List<String>

    @Query("SELECT * FROM learning_events")
    suspend fun getAll(): List<LearningEventEntity>
}

@Dao
interface KeywordRuleDao {
    @Query("SELECT * FROM keyword_rules ORDER BY category, phrase")
    fun observeRules(): Flow<List<KeywordRuleEntity>>

    @Query("SELECT * FROM keyword_rules WHERE enabled = 1 ORDER BY weight DESC")
    suspend fun getEnabledRules(): List<KeywordRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: KeywordRuleEntity)

    @Query("SELECT COUNT(*) FROM keyword_rules")
    suspend fun count(): Int

    @Query("DELETE FROM keyword_rules WHERE id = :ruleId")
    suspend fun delete(ruleId: Long)
}

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics_snapshots WHERE singletonId = 1")
    fun observeSnapshot(): Flow<AnalyticsSnapshotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AnalyticsSnapshotEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE singletonId = 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppSettingsEntity)
}
