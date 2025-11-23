package fr.unica.fetheddine.lahjaily.vibechef.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.unica.fetheddine.lahjaily.vibechef.R
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.MainViewModel
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.UiState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.draw.scale
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeChefScreen(viewModel: MainViewModel) {
    // State local pour les ingrédients et la vibe choisie
    var ingredients by remember { mutableStateOf("") }
    var selectedVibe by remember { mutableStateOf("Rapide") }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    // obtenir la valeur système hors du lambda remember (isSystemInDarkTheme est @Composable)
    val systemDark = isSystemInDarkTheme()
    var isDark by remember { mutableStateOf(systemDark) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Texte à partager basé sur l'état actuel
    val shareText = when (val state = uiState) {
        is UiState.Success -> state.recipe
        UiState.Initial -> "Aucune recette générée pour le moment."
        UiState.Loading -> "Génération en cours..."
        is UiState.Error -> "Erreur: ${state.message}"
    }

    // Launcher pour la reconnaissance vocale
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                ingredients = spokenText
            } else {
                scope.launch { snackbarHostState.showSnackbar("Aucun texte reconnu") }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Dictée annulée") }
        }
    }
    // Intent pré-configuré pour la dictée
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictez vos ingrédients")
        }
    }

    val localColorScheme = remember(isDark) { if (isDark) darkColorScheme() else lightColorScheme() }

    MaterialTheme(colorScheme = localColorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("VibeChef") },
                    actions = {
                        // Ajout: IconToggleButton pour basculer clair/sombre
                        IconToggleButton(checked = isDark, onCheckedChange = { isDark = it }) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                                contentDescription = if (isDark) "Passer en thème clair" else "Passer en thème sombre"
                            )
                        }
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Partager la recette")
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Partager"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (uiState is UiState.Success) {
                    FloatingActionButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Partager la recette"))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Partager"
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            // Ajout: couleurs explicites du Scaffold pour dark/light
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "VibeChef",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Ingrédients") },
                    placeholder = { Text("Ex: poulet, tomates, basilic...") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            try {
                                speechLauncher.launch(speechIntent)
                            } catch (_: ActivityNotFoundException) { // underscore pour ignorer le paramètre
                                scope.launch { snackbarHostState.showSnackbar("Reconnaissance vocale non disponible") }
                            }
                        }) {
                            Icon(imageVector = Icons.Filled.Mic, contentDescription = "Dicter")
                        }
                    }
                )

                // Restrictions chips multi-sélection
                val restrictionOptions = listOf("Végétarien", "Sans Gluten", "Épicé")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Restrictions", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        restrictionOptions.forEach { opt ->
                            val selected = opt in selectedFilters
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedFilters = if (selected) selectedFilters - opt else selectedFilters + opt
                                },
                                label = { Text(opt) }
                            )
                        }
                    }
                }

                // Vibes chips
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
                            viewModel.generateRecipe(ingredients, selectedVibe, selectedFilters.toList())
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cuisiner !")
                }

                when (val state = uiState) {
                    UiState.Initial -> {
                        Text(
                            text = "Entrez des ingrédients, choisissez une ambiance et des restrictions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    UiState.Loading -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "loading")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 800, easing = { it }),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "iconScale"
                        )
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1200, easing = { it }),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "textAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Chargement",
                                    modifier = Modifier.size(80.dp).scale(scale),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Le chef réfléchit...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                                )
                            }
                        }
                    }
                    is UiState.Success -> {
                        val scrollState = rememberScrollState()
                        val lines = remember(state.recipe) { state.recipe.lines() }
                        val title: String = lines.firstOrNull { it.startsWith("### ") }?.removePrefix("### ")
                            ?.trim()!!
                            .ifBlank { "Recette" }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(scrollState)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Image d'illustration en haut (directe, sans fallback)
                                Image(
                                    painter = painterResource(id = R.drawable.header_image),
                                    contentDescription = "Illustration de la recette",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(state.recipe))
                                        scope.launch { snackbarHostState.showSnackbar("Recette copiée dans le presse-papiers") }
                                    }) {
                                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copier")
                                    }
                                }
                                MarkdownText(text = state.recipe)
                            }
                        }
                    }
                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = "Erreur",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = {
                                        if (ingredients.isNotBlank()) {
                                            viewModel.generateRecipe(ingredients, selectedVibe, selectedFilters.toList())
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Veuillez saisir des ingrédients pour réessayer.") }
                                        }
                                    }
                                ) {
                                    Text(text = "Réessayer")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
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
