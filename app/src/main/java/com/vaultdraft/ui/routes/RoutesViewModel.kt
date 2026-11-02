package com.vaultdraft.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultdraft.data.repository.RouteRepository
import com.vaultdraft.domain.model.InsertionRule
import com.vaultdraft.domain.model.Route
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RoutesViewModel(private val routeRepository: RouteRepository) : ViewModel() {

    val routes: StateFlow<List<Route>> =
        routeRepository.routes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val defaultRouteId: StateFlow<String?> =
        routeRepository.defaultRouteId.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun save(
        id: String?,
        name: String,
        pathTemplate: String,
        insertionRule: InsertionRule,
        header: String,
        template: String,
        isDefault: Boolean
    ) {
        if (name.isBlank() || pathTemplate.isBlank()) return
        viewModelScope.launch {
            val route = Route(
                id = id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                pathTemplate = pathTemplate.trim(),
                insertionRule = insertionRule,
                header = header.trim(),
                template = template.ifBlank { "{{content}}" },
                isDefault = isDefault,
                createdAt = System.currentTimeMillis()
            )
            routeRepository.upsert(route)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { routeRepository.delete(id) }
    }

    fun setDefault(id: String) {
        viewModelScope.launch { routeRepository.setDefault(id) }
    }

    class Factory(private val routeRepository: RouteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RoutesViewModel(routeRepository) as T
    }
}
