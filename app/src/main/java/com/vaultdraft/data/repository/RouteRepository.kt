package com.vaultdraft.data.repository

import com.vaultdraft.data.prefs.VaultPreferences
import com.vaultdraft.domain.model.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class RouteRepository(private val prefs: VaultPreferences) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val routes: Flow<List<Route>> = prefs.routesJson.map { raw ->
        if (raw.isNullOrBlank()) emptyList() else runCatching {
            json.decodeFromString<List<Route>>(raw)
        }.getOrElse { emptyList() }
    }

    val defaultRouteId: Flow<String?> = prefs.defaultRouteId

    suspend fun upsert(route: Route) {
        val current = routes.first().toMutableList()
        val index = current.indexOfFirst { it.id == route.id }
        if (index >= 0) current[index] = route else current.add(route)
        if (route.isDefault) prefs.saveDefaultRouteId(route.id)
        persist(current)
    }

    suspend fun delete(id: String) {
        val current = routes.first().filterNot { it.id == id }
        if (defaultRouteId.first() == id) prefs.saveDefaultRouteId(current.firstOrNull()?.id.orEmpty())
        persist(current)
    }

    suspend fun setDefault(id: String) {
        prefs.saveDefaultRouteId(id)
    }

    suspend fun seedDefaultsIfEmpty() {
        if (routes.first().isNotEmpty()) return
        val now = System.currentTimeMillis()
        val defaults = listOf(
            Route(
                id = newId(), name = "Daily Note Append",
                pathTemplate = "Daily/{{YYYY-MM-DD}}.md",
                insertionRule = com.vaultdraft.domain.model.InsertionRule.APPEND,
                template = "- {{time:HH:mm}} {{content}}",
                isDefault = true, createdAt = now
            ),
            Route(
                id = newId(), name = "Idea File",
                pathTemplate = "Inbox/Ideas.md",
                insertionRule = com.vaultdraft.domain.model.InsertionRule.APPEND,
                template = "- {{content}}",
                createdAt = now
            ),
            Route(
                id = newId(), name = "Inbox Task",
                pathTemplate = "Inbox/Tasks.md",
                insertionRule = com.vaultdraft.domain.model.InsertionRule.UNDER_HEADER,
                header = "Tasks",
                template = "- [ ] {{time:HH:mm}} {{content}}",
                createdAt = now
            )
        )
        persist(defaults)
        prefs.saveDefaultRouteId(defaults.first().id)
    }

    private suspend fun persist(routes: List<Route>) {
        prefs.saveRoutesJson(json.encodeToString(routes))
    }

    private fun newId(): String = UUID.randomUUID().toString()
}
