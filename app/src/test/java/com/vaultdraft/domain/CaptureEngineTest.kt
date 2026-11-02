package com.vaultdraft.domain

import com.vaultdraft.domain.capture.CaptureEngine
import com.vaultdraft.domain.model.InsertionRule
import com.vaultdraft.domain.model.Route
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class CaptureEngineTest {

    private val now = LocalDateTime.of(2025, 3, 14, 9, 5, 7)

    private fun route(
        rule: InsertionRule = InsertionRule.APPEND,
        header: String = "",
        template: String = "{{content}}",
        path: String = "Inbox/ideas.md"
    ) = Route(
        id = "r1",
        name = "Test",
        pathTemplate = path,
        insertionRule = rule,
        header = header,
        template = template
    )

    @Test
    fun appendToEmptyFile() {
        val result = CaptureEngine.buildCapture(route(), "hello", null, now)
        assertEquals("hello\n", result.newContent)
        assertEquals("Inbox/ideas.md", result.targetPath)
    }

    @Test
    fun appendToExistingFile() {
        val result = CaptureEngine.buildCapture(route(), "second", "first\n", now)
        assertEquals("first\nsecond\n", result.newContent)
    }

    @Test
    fun prependToExistingFile() {
        val result = CaptureEngine.buildCapture(route(InsertionRule.PREPEND), "top", "bottom\n", now)
        assertEquals("top\nbottom\n", result.newContent)
    }

    @Test
    fun insertUnderExistingHeader() {
        val existing = "# Title\n\n# Tasks\n- [ ] old\n\n# Notes\ntext\n"
        val result = CaptureEngine.buildCapture(
            route(InsertionRule.UNDER_HEADER, header = "Tasks"), "- [ ] new", existing, now
        )
        assertEquals("# Title\n\n# Tasks\n- [ ] new\n- [ ] old\n\n# Notes\ntext\n", result.newContent)
    }

    @Test
    fun insertUnderMissingHeaderCreatesIt() {
        val result = CaptureEngine.buildCapture(
            route(InsertionRule.UNDER_HEADER, header = "Log"), "entry", "# Doc\n", now
        )
        assertEquals("# Doc\n# Log\nentry\n", result.newContent)
    }

    @Test
    fun templateTokensAreExpanded() {
        val result = CaptureEngine.buildCapture(
            route(template = "- [ ] {{time:HH:mm}} {{content}}"), "buy milk", null, now
        )
        assertEquals("- [ ] 09:05 buy milk\n", result.newContent)
    }

    @Test
    fun dateTokensInPathAreExpanded() {
        val result = CaptureEngine.buildCapture(
            route(path = "Daily/{{YYYY-MM-DD}}.md"), "note", null, now
        )
        assertEquals("Daily/2025-03-14.md", result.targetPath)
    }
}
