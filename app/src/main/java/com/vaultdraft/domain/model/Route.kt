package com.vaultdraft.domain.model

import kotlinx.serialization.Serializable

enum class InsertionRule {
    APPEND,
    PREPEND,
    UNDER_HEADER
}

@Serializable
data class Route(
    val id: String,
    val name: String,
    val pathTemplate: String,
    val insertionRule: InsertionRule = InsertionRule.APPEND,
    val header: String = "",
    val template: String = "{{content}}",
    val isDefault: Boolean = false,
    val createdAt: Long = 0L
) {
    fun isValid(): Boolean = name.isNotBlank() && pathTemplate.isNotBlank()

    fun requiresHeader(): Boolean =
        insertionRule == InsertionRule.UNDER_HEADER && header.isNotBlank()
}
