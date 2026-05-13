package com.rishabh.todo.codex.data.di

import android.content.Context
import androidx.room.Room
import com.rishabh.todo.codex.data.local.AppDatabase
import com.rishabh.todo.codex.data.repository.AnalyticsRepositoryImpl
import com.rishabh.todo.codex.data.repository.ContactPolicyRepositoryImpl
import com.rishabh.todo.codex.data.repository.ExportRepositoryImpl
import com.rishabh.todo.codex.data.repository.LearningRepositoryImpl
import com.rishabh.todo.codex.data.repository.NotificationRepositoryImpl
import com.rishabh.todo.codex.data.repository.SettingsRepositoryImpl
import com.rishabh.todo.codex.data.repository.TaskRepositoryImpl
import com.rishabh.todo.codex.data.reminder.WorkManagerReminderScheduler
import com.rishabh.todo.codex.domain.repository.AnalyticsRepository
import com.rishabh.todo.codex.domain.repository.ContactPolicyRepository
import com.rishabh.todo.codex.domain.repository.ExportRepository
import com.rishabh.todo.codex.domain.repository.LearningRepository
import com.rishabh.todo.codex.domain.repository.NotificationRepository
import com.rishabh.todo.codex.domain.repository.SettingsRepository
import com.rishabh.todo.codex.domain.repository.TaskRepository
import com.rishabh.todo.codex.domain.engine.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "offline_task_manager.db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides fun provideTaskDao(db: AppDatabase) = db.taskDao()
    @Provides fun provideNotificationDao(db: AppDatabase) = db.notificationDao()
    @Provides fun provideContactDao(db: AppDatabase) = db.contactDao()
    @Provides fun provideLearningDao(db: AppDatabase) = db.learningDao()
    @Provides fun provideAnalyticsDao(db: AppDatabase) = db.analyticsDao()
    @Provides fun provideSettingsDao(db: AppDatabase) = db.settingsDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
    @Binds abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
    @Binds abstract fun bindContactPolicyRepository(impl: ContactPolicyRepositoryImpl): ContactPolicyRepository
    @Binds abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
    @Binds abstract fun bindExportRepository(impl: ExportRepositoryImpl): ExportRepository
    @Binds abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds abstract fun bindLearningRepository(impl: LearningRepositoryImpl): LearningRepository
    @Binds abstract fun bindReminderScheduler(impl: WorkManagerReminderScheduler): ReminderScheduler
}
