package fr.unica.fetheddine.lahjaily.vibechef.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Renders a subset of Markdown as Compose UI:
 *  - # H1 (skipped — title handled elsewhere)
 *  - ## H2 → headlineSmall
 *  - ### H3 → titleMedium
 *  - Bullet lists: - item
 *  - Numbered lists: 1. step
 *  - Bold: **text**
 *  - Italic: *text*
 *  - Blank lines → spacing
 */
@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val lines = remember(text) { text.lines() }
        lines.forEach { rawLine ->
            val line = rawLine.trimEnd()
            when {
                line.startsWith("# ") -> { /* Skip: global title handled elsewhere */ }

                line.startsWith("## ") -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = buildStyledAnnotated(line.removePrefix("## ").trim()),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                line.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildStyledAnnotated(line.removePrefix("### ").trim()),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                line.isBlank() -> Spacer(modifier = Modifier.height(6.dp))

                line.trimStart().startsWith("- ") -> {
                    val indent = line.length - line.trimStart().length
                    Row(modifier = Modifier.padding(start = (indent * 4 + 8).dp)) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildStyledAnnotated(line.trimStart().removePrefix("- ")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                line.trimStart().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val trimmed = line.trimStart()
                    val numberEnd = trimmed.indexOf(". ")
                    val number = trimmed.substring(0, numberEnd + 1)
                    val content = trimmed.substring(numberEnd + 2)
                    Row(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "$number ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = buildStyledAnnotated(content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                else -> {
                    Text(
                        text = buildStyledAnnotated(line),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Parses inline Markdown formatting: **bold** and *italic*
 */
@Composable
fun buildStyledAnnotated(input: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < input.length) {
            when {
                // Bold: **text**
                i + 1 < input.length && input[i] == '*' && input[i + 1] == '*' -> {
                    val end = input.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(input.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append(input[i])
                        i++
                    }
                }
                // Italic: *text* (single asterisk, not followed by another)
                input[i] == '*' && (i + 1 >= input.length || input[i + 1] != '*') -> {
                    val end = input.indexOf('*', i + 1)
                    if (end != -1 && (end + 1 >= input.length || input[end + 1] != '*')) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(input.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append(input[i])
                        i++
                    }
                }
                else -> {
                    append(input[i])
                    i++
                }
            }
        }
    }
}
