package com.vaultdraft.ui.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onNavigateToRoutes: () -> Unit
) {
    val vaultUri by viewModel.vaultUri.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val status by viewModel.status.collectAsState()

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(draft) {
        if (textFieldValue.text != draft) {
            textFieldValue = TextFieldValue(draft, TextRange(draft.length))
        }
    }

    LaunchedEffect(status) {
        when (val s = status) {
            is CaptureViewModel.Status.Sent -> snackbarHostState.showSnackbar("Saved to ${s.path}")
            is CaptureViewModel.Status.Error -> snackbarHostState.showSnackbar(s.message)
            else -> Unit
        }
        if (status != CaptureViewModel.Status.Idle) viewModel.consumeStatus()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VaultDraft") },
                actions = {
                    IconButton(onClick = { viewModel.pickVault() }) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = "Choose vault folder")
                    }
                    IconButton(onClick = onNavigateToRoutes) {
                        Icon(Icons.Filled.Settings, contentDescription = "Routes")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (vaultUri == null) {
                OutlinedButton(onClick = { viewModel.pickVault() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Select Obsidian vault folder")
                }
            } else {
                Text(
                    text = "Vault linked",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            RouteSelector(
                routes = routes,
                selected = selectedRoute,
                onSelected = { viewModel.onRouteSelected(it.id) }
            )

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    viewModel.onDraftChange(it.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .focusRequester(focusRequester),
                placeholder = { Text("Capture to your vault...") },
                minLines = 6
            )

            FormattingBar(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    viewModel.onDraftChange(it.text)
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    SpeechButton(onResult = { viewModel.appendCaptured(it) })
                    OcrButton(
                        onResult = { viewModel.appendCaptured(it) },
                        onError = { msg -> viewModel.onDraftChange(viewModel.draft.value + "\n[OCR error: $msg]") }
                    )
                }
                Button(
                    onClick = { viewModel.send() },
                    enabled = status !is CaptureViewModel.Status.Sending
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Text("  Send", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteSelector(
    routes: List<com.vaultdraft.domain.model.Route>,
    selected: com.vaultdraft.domain.model.Route?,
    onSelected: (com.vaultdraft.domain.model.Route) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "No routes",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            routes.forEach { route ->
                DropdownMenuItem(
                    text = { Text(route.name + if (route.isDefault) "  (default)" else "") },
                    onClick = {
                        onSelected(route)
                        expanded = false
                    }
                )
            }
        }
    }
}
