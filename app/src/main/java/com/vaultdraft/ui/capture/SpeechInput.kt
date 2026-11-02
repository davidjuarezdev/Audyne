package com.vaultdraft.ui.capture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale

@Composable
fun rememberSpeechInputLauncher(onResult: (String) -> Unit): () -> Unit {
    var launcherHolder by remember { mutableStateOf<((Intent) -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = matches?.firstOrNull().orEmpty()
        if (text.isNotBlank()) onResult(text)
    }
    launcherHolder = { intent -> launcher.launch(intent) }
    return {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to capture")
        }
        launcherHolder?.invoke(intent)
    }
}

fun speechRecognizerAvailable(context: Context): Boolean {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    return context.packageManager.resolveActivity(intent, 0) != null
}

@Composable
fun SpeechButton(onResult: (String) -> Unit) {
    val launcher = rememberSpeechInputLauncher(onResult)
    IconButton(onClick = launcher) {
        Icon(imageVector = Icons.Filled.Mic, contentDescription = "Speech to text")
    }
}
