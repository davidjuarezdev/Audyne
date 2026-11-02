package com.vaultdraft

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.vaultdraft.ui.theme.VaultDraftTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CaptureActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ROUTE_ID = "com.vaultdraft.EXTRA_ROUTE_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val routeId = intent?.getStringExtra(EXTRA_ROUTE_ID)

        setContent {
            VaultDraftTheme {
                var text by remember { mutableStateOf("") }
                var status by remember { mutableStateOf<String?>(null) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Quick capture", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type or paste...") },
                        minLines = 4
                    )
                    Button(
                        onClick = {
                            val app = application as VaultDraftApp
                            lifecycleScope.launch {
                                val uri = app.preferences.vaultUri.first()
                                val routes = app.routeRepository.routes.first()
                                val route = routes.firstOrNull { it.id == routeId } ?: routes.firstOrNull()
                                status = when {
                                    uri == null -> "Select a vault folder in the app first."
                                    route == null -> "No routes configured."
                                    text.isBlank() -> "Nothing to save."
                                    else -> runCatching {
                                        val result = app.vaultRepository.writeCapture(uri, route, text.trim())
                                        "Saved to ${result.targetPath}"
                                    }.getOrElse { it.message ?: "Write failed" }
                                }
                                if (status?.startsWith("Saved") == true) {
                                    text = ""
                                    finish()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send to vault")
                    }
                    if (status != null && !status!!.startsWith("Saved")) {
                        Text(status!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
