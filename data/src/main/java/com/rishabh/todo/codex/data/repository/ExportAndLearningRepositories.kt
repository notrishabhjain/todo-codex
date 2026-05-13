package com.rishabh.todo.codex.data.repository

import android.content.Context
import com.rishabh.todo.codex.data.local.KeywordRuleDao
import com.rishabh.todo.codex.data.local.LearningDao
import com.rishabh.todo.codex.data.local.TaskDao
import com.rishabh.todo.codex.data.local.toDomain
import com.rishabh.todo.codex.data.local.toEntity
import com.rishabh.todo.codex.domain.model.KeywordRule
import com.rishabh.todo.codex.domain.model.LearningEvent
import com.rishabh.todo.codex.domain.repository.ExportRepository
import com.rishabh.todo.codex.domain.repository.KeywordRuleRepository
import com.rishabh.todo.codex.domain.repository.LearningRepository
import java.io.File
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map

class LearningRepositoryImpl @Inject constructor(
    private val dao: LearningDao,
) : LearningRepository {
    override suspend fun record(event: LearningEvent) = dao.insert(event.toEntity())
    override suspend fun getAcceptedPhrases(): List<String> = dao.getAcceptedPhrases()
    override suspend fun getRejectedPhrases(): List<String> = dao.getRejectedPhrases()
}

class KeywordRuleRepositoryImpl @Inject constructor(
    private val dao: KeywordRuleDao,
) : KeywordRuleRepository {
    override fun observeRules() = dao.observeRules().map { rows -> rows.map { it.toDomain() } }
    override suspend fun getEnabledRules(): List<KeywordRule> = dao.getEnabledRules().map { it.toDomain() }
    override suspend fun upsert(rule: KeywordRule) = dao.upsert(rule.toEntity())
    override suspend fun count(): Int = dao.count()
}

class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskDao: TaskDao,
) : ExportRepository {
    private val json = Json { prettyPrint = true }

    override suspend fun exportJson(destinationPath: String): Result<String> = runCatching {
        val payload = taskDao.getAll().map { it.toDomain() }
        val output = resolveOutputFile(destinationPath)
        output.writeText(json.encodeToString(payload))
        output.absolutePath
    }

    override suspend fun exportCsv(destinationPath: String): Result<String> = runCatching {
        val lines = buildList {
            add("id,title,description,source,sender,due,priority,status")
            taskDao.getAll().forEach { task ->
                add(
                    listOf(
                        task.id,
                        task.title.escapeCsv(),
                        task.description.escapeCsv(),
                        task.sourceType,
                        task.sender.orEmpty().escapeCsv(),
                        task.dueAtEpochMillis ?: "",
                        task.priority,
                        task.status,
                    ).joinToString(","),
                )
            }
        }
        val output = resolveOutputFile(destinationPath)
        output.writeText(lines.joinToString("\n"))
        output.absolutePath
    }

    override suspend fun importJson(sourcePath: String): Result<Int> = runCatching {
        val file = File(sourcePath)
        val tasks = json.decodeFromString<List<com.rishabh.todo.codex.domain.model.Task>>(file.readText())
        tasks.forEach { taskDao.upsert(it.toEntity()) }
        tasks.size
    }

    private fun String.escapeCsv(): String = "\"${replace("\"", "\"\"")}\""

    private fun resolveOutputFile(path: String): File {
        val file = File(path)
        return if (file.isAbsolute) file else File(context.filesDir, path)
    }
}
