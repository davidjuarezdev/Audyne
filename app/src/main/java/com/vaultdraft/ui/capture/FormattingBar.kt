package com.vaultdraft.ui.capture

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

data class FormatAction(val label: String, val wrap: Pair<String, String>, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val actions = listOf(
    FormatAction("Bold", "**" to "**", Icons.Filled.FormatBold),
    FormatAction("Italic", "*" to "*", Icons.Filled.FormatItalic),
    FormatAction("Strikethrough", "~~" to "~~", Icons.Filled.FormatStrikethrough),
    FormatAction("Code", "`" to "`", Icons.Filled.Code),
    FormatAction("Quote", "> " to "", Icons.Filled.FormatQuote),
    FormatAction("List", "- " to "", Icons.Filled.FormatListBulleted),
    FormatAction("Link", "[" to "](https://)", Icons.Filled.Link)
)

@Composable
fun FormattingBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp)
    ) {
        actions.forEach { action ->
            IconButton(onClick = {
                val selection = value.selection
                val selected = value.text.substring(selection.start, selection.end)
                val (prefix, suffix) = action.wrap
                val replacement = if (action.label == "List" || action.label == "Quote") {
                    selected.lines().joinToString("\n") { prefix + it }
                } else {
                    prefix + selected + suffix
                }
                val newText = value.text.replaceRange(selection.start, selection.end, replacement)
                val newCursor = if (selected.isEmpty()) {
                    selection.start + prefix.length
                } else {
                    selection.end + prefix.length + suffix.length
                }
                onValueChange(value.copy(text = newText, selection = TextRange(newCursor)))
            }) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
