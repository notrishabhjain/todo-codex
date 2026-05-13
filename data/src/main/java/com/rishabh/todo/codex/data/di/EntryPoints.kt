package com.rishabh.todo.codex.data.di

import com.rishabh.todo.codex.domain.repository.AnalyticsRepository
import com.rishabh.todo.codex.domain.repository.ExportRepository
import com.rishabh.todo.codex.domain.repository.SettingsRepository
import com.rishabh.todo.codex.domain.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TaskRepositoryEntryPoint {
    fun taskRepository(): TaskRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackgroundDependenciesEntryPoint {
    fun settingsRepository(): SettingsRepository
    fun exportRepository(): ExportRepository
    fun analyticsRepository(): AnalyticsRepository
}
