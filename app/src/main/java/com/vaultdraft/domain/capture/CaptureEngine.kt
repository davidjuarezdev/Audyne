package com.vaultdraft.domain.capture

import com.vaultdraft.domain.model.InsertionRule
import com.vaultdraft.domain.model.Route
import com.vaultdraft.domain.tokens.TokenEngine
import java.time.LocalDateTime

object CaptureEngine {

    data class Result(
        val targetPath: String,
        val newContent: String
    )

    fun buildCapture(
        route: Route,
        content: String,
        existingFileContent: String?,
        now: LocalDateTime = LocalDateTime.now()
    ): Result {
        val path = TokenEngine.expand(route.pathTemplate, now)
        val line = TokenEngine.expand(route.template, now)
            .replace("{{content}}", content)
            .trimEnd('\n')

        val newContent = when (route.insertionRule) {
            InsertionRule.APPEND -> append(existingFileContent, line)
            InsertionRule.PREPEND -> prepend(existingFileContent, line)
            InsertionRule.UNDER_HEADER -> insertUnderHeader(existingFileContent, route.header, line)
        }
        return Result(targetPath = path, newContent = newContent)
    }

    private fun append(existing: String?, line: String): String {
        val base = existing?.trimEnd('\n') ?: ""
        return if (base.isEmpty()) {
            line + "\n"
        } else {
            base + "\n" + line + "\n"
        }
    }

    private fun prepend(existing: String?, line: String): String {
        val base = existing?.trimEnd('\n') ?: ""
        return if (base.isEmpty()) {
            line + "\n"
        } else {
            line + "\n" + base + "\n"
        }
    }

    private fun insertUnderHeader(existing: String?, header: String, line: String): String {
        val base = existing ?: ""
        val headerRegex = Regex("(?m)^#{1,6}\\s*${Regex.escape(header)}\\s*$")
        if (base.isEmpty() || headerRegex.find(base) == null) {
            val block = "# $header\n$line\n"
            return if (base.isEmpty()) block else append(base, block.trimEnd('\n'))
        }
        val match = headerRegex.find(base)!!
        val insertAt = match.range.last + 1
        val remainder = base.substring(insertAt).trimStart('\n')
        val prefix = base.substring(0, insertAt).trimEnd('\n')
        return if (remainder.isEmpty()) {
            "$prefix\n$line\n"
        } else {
            "$prefix\n$line\n$remainder"
        }
    }
}
