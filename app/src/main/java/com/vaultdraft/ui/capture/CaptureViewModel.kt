package com.vaultdraft.ui.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaultdraft.data.prefs.VaultPreferences
import com.vaultdraft.data.repository.RouteRepository
import com.vaultdraft.data.saf.VaultRepository
import com.vaultdraft.domain.model.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CaptureViewModel(
    private val prefs: VaultPreferences,
    private val routeRepository: RouteRepository,
    private val vaultRepository: VaultRepository,
    private val onPickVault: () -> Unit,
    initialRouteId: String? = null
) : ViewModel() {

    val vaultUri: StateFlow<Uri?> = prefs.vaultUri.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val routes: StateFlow<List<Route>> = routeRepository.routes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedRouteId = MutableStateFlow<String?>(initialRouteId)
    val selectedRoute: StateFlow<Route?> =
        combine(routes, _selectedRouteId, routeRepository.defaultRouteId) { list, selected, defaultId ->
            list.firstOrNull { it.id == selected }
                ?: list.firstOrNull { it.id == defaultId }
                ?: list.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft

    private val _status = MutableStateFlow<Status>(Status.Idle)
    val status: StateFlow<Status> = _status

    init {
        if (initialRouteId == null) {
            viewModelScope.launch {
                routeRepository.defaultRouteId.value?.let { _selectedRouteId.value = it }
            }
        }
    }

    fun onDraftChange(value: String) {
        _draft.value = value
    }

    fun onRouteSelected(id: String) {
        _selectedRouteId.value = id
    }

    fun pickVault() = onPickVault()

    fun appendCaptured(text: String) {
        _draft.value = if (_draft.value.isEmpty()) text else _draft.value + "\n" + text
    }

    fun send() {
        val route = selectedRoute.value
        val content = _draft.value.trim()
        val uri = vaultUri.value
        when {
            route == null -> _status.value = Status.Error("No route configured. Create one first.")
            content.isEmpty() -> _status.value = Status.Error("Nothing to save.")
            uri == null || !vaultRepository.hasAccess(uri) -> _status.value = Status.Error("Pick your Obsidian vault folder first.")
            else -> viewModelScope.launch {
                _status.value = Status.Sending
                runCatching { vaultRepository.writeCapture(uri, route, content) }
                    .onSuccess {
                        _status.value = Status.Sent(it.targetPath)
                        _draft.value = ""
                    }
                    .onFailure { _status.value = Status.Error(it.message ?: "Write failed") }
            }
        }
    }

    fun consumeStatus() {
        _status.value = Status.Idle
    }

    sealed interface Status {
        data object Idle : Status
        data object Sending : Status
        data class Sent(val path: String) : Status
        data class Error(val message: String) : Status
    }

    class Factory(
        private val prefs: VaultPreferences,
        private val routeRepository: RouteRepository,
        private val vaultRepository: VaultRepository,
        private val onPickVault: () -> Unit,
        private val initialRouteId: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CaptureViewModel(prefs, routeRepository, vaultRepository, onPickVault, initialRouteId) as T
    }
}
