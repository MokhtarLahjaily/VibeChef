package fr.unica.fetheddine.lahjaily.vibechef.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

/**
 * Affiche un sous-ensemble simple de Markdown:
 *  - Lignes commençant par "# " ignorées (titre global déjà géré ailleurs)
 *  - Lignes commençant par "### " rendues comme titreMedium
 *  - Gras inline: **texte**
 *  - Ligne vide => petit espace
 */
@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val lines = remember(text) { text.lines() }
        lines.forEach { rawLine ->
            val line = rawLine.trimEnd()
            when {
                line.startsWith("# ") -> { /* Ignoré: titre global */ }
                line.startsWith("### ") -> {
                    val title = line.removePrefix("### ").trim()
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.isBlank() -> Spacer(modifier = Modifier.height(4.dp))
                else -> {
                    Text(
                        text = buildBoldAnnotated(line),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun buildBoldAnnotated(input: String): AnnotatedString {
    val regex = Regex("\\*\\*(.+?)\\*\\*")
    return buildAnnotatedString {
        var lastIndex = 0
        regex.findAll(input).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            append(input.substring(lastIndex, start))
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(match.groupValues[1])
            pop()
            lastIndex = end
        }
        if (lastIndex < input.length) {
            append(input.substring(lastIndex))
        }
    }
}
