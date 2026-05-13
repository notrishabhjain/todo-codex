package com.rishabh.todo.codex.data.bootstrap

import android.content.Context
import com.rishabh.todo.codex.domain.model.KeywordRule
import com.rishabh.todo.codex.domain.repository.KeywordRuleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class KeywordRuleSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keywordRuleRepository: KeywordRuleRepository,
) {
    suspend fun seedIfEmpty() {
        if (keywordRuleRepository.count() > 0) return
        val payload = context.assets.open("seed_keywords.json").bufferedReader().use { it.readText() }
        val rules = Json.decodeFromString<List<SeedKeywordRule>>(payload)
        rules.forEach { rule ->
            keywordRuleRepository.upsert(
                KeywordRule(
                    phrase = rule.phrase,
                    category = rule.category,
                    weight = rule.weight,
                    languageHint = rule.languageHint,
                    enabled = true,
                ),
            )
        }
    }
}

@Serializable
private data class SeedKeywordRule(
    val phrase: String,
    val category: String,
    val weight: Float,
    val languageHint: String,
)
