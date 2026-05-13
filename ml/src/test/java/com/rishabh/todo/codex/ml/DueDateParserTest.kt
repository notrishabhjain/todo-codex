package com.rishabh.todo.codex.ml

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertNotNull

class DueDateParserTest {
    @Test
    fun parsesKalPhrase() {
        val result = DueDateParser.parse("bhai kal tak proposal bhej dena", Instant.parse("2026-05-12T10:00:00Z"))
        assertNotNull(result)
    }
}
