package com.vaultdraft.domain.tokens

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TokenEngine {

    private val tokenPattern = Regex("\\{\\{([^}]+)\\}\\}")

    fun expand(input: String, now: LocalDateTime = LocalDateTime.now()): String {
        val zone: ZoneId = ZoneId.systemDefault()
        return tokenPattern.replace(input) { match ->
            val raw = match.groupValues[1].trim()
            resolve(raw, now, zone) ?: match.value
        }
    }

    private fun resolve(raw: String, now: LocalDateTime, zone: ZoneId): String? {
        val parts = raw.split(":", limit = 2)
        val key = parts[0].uppercase(Locale.ROOT)
        val arg = parts.getOrNull(1)
        return when {
            key.startsWith("DATETIME") -> now.format(patternOrDefault(arg, "yyyy-MM-dd HH:mm"))
            key.startsWith("TIMESTAMP") -> now.atZone(zone).toEpochSecond().toString()
            key.startsWith("DATE") -> now.format(patternOrDefault(arg, "yyyy-MM-dd"))
            key.startsWith("TIME") -> now.format(patternOrDefault(arg, "HH:mm"))
            key.any { it in "YMD" } -> now.format(patternOrDefault(raw, "yyyy-MM-dd"))
            key.any { it in "HMS" } -> now.format(patternOrDefault(raw, "HH:mm"))
            else -> null
        }
    }

    private fun patternOrDefault(pattern: String?, default: String): DateTimeFormatter {
        val p = pattern?.takeIf { it.isNotBlank() } ?: default
        return try {
            DateTimeFormatter.ofPattern(p, Locale.ROOT)
        } catch (e: IllegalArgumentException) {
            DateTimeFormatter.ofPattern(default, Locale.ROOT)
        }
    }

    fun containsDateTokens(input: String): Boolean =
        tokenPattern.findAll(input).any {
            val key = it.groupValues[1].trim().uppercase(Locale.ROOT)
            key.startsWith("DATE") || key.any { c -> c in "YMD" }
        }
}
