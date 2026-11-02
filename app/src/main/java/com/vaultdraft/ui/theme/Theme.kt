package com.vaultdraft.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaultDarkScheme = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    secondary = Color(0xFF67E8F9),
    onSecondary = Color(0xFF083344),
    background = Color(0xFF0B0F19),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE5E7EB),
    onBackground = Color(0xFFE5E7EB)
)

@Composable
fun VaultDraftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VaultDarkScheme,
        content = content
    )
}
