package com.vaultdraft.domain

import com.vaultdraft.domain.tokens.TokenEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class TokenEngineTest {

    private val now = LocalDateTime.of(2025, 3, 14, 9, 5, 7)

    @Test
    fun expandsIsoDate() {
        assertEquals("Daily/2025-03-14.md", TokenEngine.expand("Daily/{{YYYY-MM-DD}}.md", now))
    }

    @Test
    fun expandsBareDateAndTimePatterns() {
        assertEquals("20250314", TokenEngine.expand("{{yyyyMMdd}}", now))
        assertEquals("09:05:07", TokenEngine.expand("{{HH:mm:ss}}", now))
    }

    @Test
    fun expandsCustomDatePattern() {
        assertEquals("2025/03/14", TokenEngine.expand("{{date:yyyy/MM/dd}}", now))
    }

    @Test
    fun expandsTimeAndDatetime() {
        assertEquals("09:05", TokenEngine.expand("{{time:HH:mm}}", now))
        assertEquals("2025-03-14 09:05", TokenEngine.expand("{{datetime}}", now))
    }

    @Test
    fun expandsTimestamp() {
        val expected = now.atZone(java.time.ZoneId.systemDefault()).toEpochSecond().toString()
        assertEquals(expected, TokenEngine.expand("{{timestamp}}", now))
    }

    @Test
    fun leavesUnknownTokensUntouched() {
        assertEquals("a {{foo}} b", TokenEngine.expand("a {{foo}} b", now))
    }

    @Test
    fun detectsDateTokens() {
        assertTrue(TokenEngine.containsDateTokens("Daily/{{YYYY-MM-DD}}.md"))
        assertFalse(TokenEngine.containsDateTokens("Inbox/ideas.md"))
    }
}
