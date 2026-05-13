package com.rishabh.todo.codex.ml

import com.rishabh.todo.codex.domain.model.NotificationRecord
import com.rishabh.todo.codex.domain.model.SourceType
import com.rishabh.todo.codex.domain.repository.ContactPolicyRepository
import com.rishabh.todo.codex.domain.repository.KeywordRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class HybridExtractionEngineTest {
    @Test
    fun marksActionableNotification() = runTest {
        val engine = HybridExtractionEngine(
            FakeContactPolicyRepository(),
            FakeKeywordRuleRepository(),
        )
        val result = engine.extract(
            NotificationRecord(
                packageName = "com.whatsapp",
                sourceType = SourceType.WHATSAPP,
                sender = "Amit",
                title = "Amit",
                body = "Send the proposal kal",
                conversationType = "SINGLE",
                rawText = "Send the proposal kal",
                receivedAtEpochMillis = 0L,
            ),
        )
        assertTrue(result.actionable)
    }
}

private class FakeContactPolicyRepository : ContactPolicyRepository {
    override fun observeContacts(): Flow<List<com.rishabh.todo.codex.domain.model.ContactProfile>> = emptyFlow()
    override suspend fun upsert(contactProfile: com.rishabh.todo.codex.domain.model.ContactProfile) = Unit
    override suspend fun getByName(name: String) = null
}

private class FakeKeywordRuleRepository : KeywordRuleRepository {
    override fun observeRules(): Flow<List<com.rishabh.todo.codex.domain.model.KeywordRule>> = emptyFlow()
    override suspend fun getEnabledRules(): List<com.rishabh.todo.codex.domain.model.KeywordRule> = emptyList()
    override suspend fun upsert(rule: com.rishabh.todo.codex.domain.model.KeywordRule) = Unit
    override suspend fun count(): Int = 0
    override suspend fun delete(ruleId: Long) = Unit
}
