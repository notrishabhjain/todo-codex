package com.rishabh.todo.codex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAtEpochMillis DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY dueAtEpochMillis IS NULL, dueAtEpochMillis ASC")
    fun observePendingTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TaskEntity): Long

    @Update
    suspend fun update(entity: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun delete(taskId: Long)

    @Query("UPDATE tasks SET status = 'COMPLETED' WHERE id = :taskId")
    suspend fun complete(taskId: Long)

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
