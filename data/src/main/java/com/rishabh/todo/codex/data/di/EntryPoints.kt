package com.rishabh.todo.codex.data.di

import com.rishabh.todo.codex.domain.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TaskRepositoryEntryPoint {
    fun taskRepository(): TaskRepository
}
