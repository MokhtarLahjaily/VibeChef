package fr.unica.fetheddine.lahjaily.vibechef.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.MainViewModel
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.UiState

@Composable
fun VibeChefScreen(viewModel: MainViewModel) {
    // State local pour les ingrédients et la vibe choisie
    var ingredients by remember { mutableStateOf("") }
    var selectedVibe by remember { mutableStateOf("Rapide") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "VibeChef",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = ingredients,
            onValueChange = { ingredients = it },
            label = { Text("Ingrédients") },
            placeholder = { Text("Ex: poulet, tomates, basilic...") },
            modifier = Modifier.fillMaxWidth()
        )

        // Chips pour la vibe
        val vibes = listOf("Rapide", "Gourmet", "Fun")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            vibes.forEach { vibe ->
                FilterChip(
                    selected = selectedVibe == vibe,
                    onClick = { selectedVibe = vibe },
                    label = { Text(vibe) }
                )
            }
        }

        Button(
            onClick = {
                if (ingredients.isNotBlank()) {
                    viewModel.generateRecipe(ingredients, selectedVibe)
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Cuisiner !")
        }

        when (val state = uiState) {
            UiState.Initial -> {
                Text(
                    text = "Entrez des ingrédients et choisissez une ambiance pour générer une recette.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            UiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                val scrollState = rememberScrollState()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MarkdownText(text = state.recipe)
                    }
                }
            }
            is UiState.Error -> {
                Text(
                    text = state.message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Composable simple pour rendre un sous-ensemble de Markdown :
// - Titres niveau 3: lignes commençant par "### "
// - Gras inline: **texte**
@Composable
private fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val lines = remember(text) { text.lines() }
        lines.forEach { rawLine ->
            val line = rawLine.trimEnd()
            when {
                line.startsWith("### ") -> {
                    val title = line.removePrefix("### ").trim()
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(text = buildBoldAnnotated(line), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun buildBoldAnnotated(input: String): AnnotatedString {
    // Parse très basique du gras **...** sans gestion des échappements complexes
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
