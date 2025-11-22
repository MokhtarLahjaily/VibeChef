package fr.unica.fetheddine.lahjaily.vibechef.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                        Text(
                            text = state.recipe,
                            style = MaterialTheme.typography.bodyMedium
                        )
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

