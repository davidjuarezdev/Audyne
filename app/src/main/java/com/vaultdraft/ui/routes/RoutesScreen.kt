package com.vaultdraft.ui.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaultdraft.domain.model.InsertionRule
import com.vaultdraft.domain.model.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    viewModel: RoutesViewModel,
    onBack: () -> Unit
) {
    val routes by viewModel.routes.collectAsState()
    val defaultId by viewModel.defaultRouteId.collectAsState()
    var editorFor by remember { mutableStateOf<Route?>(null) }
    var editorOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editorFor = null; editorOpen = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New route")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(routes, key = { it.id }) { route ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(route.name, style = MaterialTheme.typography.titleMedium)
                            Text(route.pathTemplate, style = MaterialTheme.typography.bodySmall)
                            Text(route.insertionRule.name + if (route.requiresHeader()) " -> #${route.header}" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary)
                        }
                        Row {
                            IconButton(onClick = { viewModel.setDefault(route.id) }) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Set default",
                                    tint = if (route.id == defaultId) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { editorFor = route; editorOpen = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.delete(route.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (editorOpen) {
        RouteEditorDialog(
            route = editorFor,
            onDismiss = { editorOpen = false },
            onSave = { id, name, path, rule, header, template, isDefault ->
                viewModel.save(id, name, path, rule, header, template, isDefault)
                editorOpen = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteEditorDialog(
    route: Route?,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, path: String, rule: InsertionRule, header: String, template: String, isDefault: Boolean) -> Unit
) {
    var name by remember(route) { mutableStateOf(route?.name.orEmpty()) }
    var path by remember(route) { mutableStateOf(route?.pathTemplate.orEmpty()) }
    var rule by remember(route) { mutableStateOf(route?.insertionRule ?: InsertionRule.APPEND) }
    var header by remember(route) { mutableStateOf(route?.header.orEmpty()) }
    var template by remember(route) { mutableStateOf(route?.template ?: "{{content}}") }
    var isDefault by remember(route) { mutableStateOf(route?.isDefault ?: false) }
    var ruleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (route == null) "New route" else "Edit route") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(path, { path = it }, label = { Text("Path (date tokens: {{YYYY-MM-DD}})") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = ruleExpanded, onExpandedChange = { ruleExpanded = !ruleExpanded }) {
                    OutlinedTextField(
                        value = rule.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Insertion rule") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ruleExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = ruleExpanded, onDismissRequest = { ruleExpanded = false }) {
                        InsertionRule.entries.forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { rule = r; ruleExpanded = false })
                        }
                    }
                }
                if (rule == InsertionRule.UNDER_HEADER) {
                    OutlinedTextField(header, { header = it }, label = { Text("Markdown header") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(template, { template = it }, label = { Text("Template ({{content}}, {{time:HH:mm}})") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Default route")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(route?.id, name, path, rule, header, template, isDefault) }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
