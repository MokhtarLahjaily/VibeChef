package fr.unica.fetheddine.lahjaily.vibechef.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.unica.fetheddine.lahjaily.vibechef.R
import fr.unica.fetheddine.lahjaily.vibechef.ui.components.MarkdownText
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.MainViewModel
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.UiState
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeChefScreen(
    viewModel: MainViewModel,
    userId: String,
    onSignOut: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    var ingredients by remember { mutableStateOf("") }
    var selectedVibe by remember { mutableStateOf(context.getString(R.string.vibe_fast)) }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    val capturedImages = remember { mutableStateListOf<Bitmap>() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsState() // observe global theme state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard: ClipboardManager = remember(context) {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    val shareText by remember(uiState) {
        derivedStateOf {
            when (val state = uiState) {
                is UiState.Success -> state.recipe
                UiState.Initial -> context.getString(R.string.status_no_recipe)
                UiState.Loading -> context.getString(R.string.status_loading)
                is UiState.Error -> context.getString(R.string.status_error, state.message)
            }
        }
    }

    // Reconnaissance vocale
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                ingredients = spokenText
            } else {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_no_speech_text)) }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_speech_cancelled)) }
        }
    }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.speech_prompt))
        }
    }

    // Caméra (prévisualisation)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) capturedImages.add(bitmap)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_app)) },
                actions = {
                    IconToggleButton(checked = isDark, onCheckedChange = { viewModel.toggleTheme() }) {
                        val cd = if (isDark) stringResource(R.string.desc_theme_toggle_light) else stringResource(R.string.desc_theme_toggle_dark)
                        Icon(
                            imageVector = if (isDark) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                            contentDescription = cd
                        )
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            Intent.createChooser(intent, context.getString(R.string.desc_share_full))
                        )
                    }) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = stringResource(R.string.desc_share))
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(imageVector = Icons.Filled.History, contentDescription = "Historique")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.action_logout))
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
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.desc_share_full)))
                }) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = stringResource(R.string.desc_share))
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text(stringResource(R.string.label_ingredients)) },
                placeholder = { Text(stringResource(R.string.placeholder_ingredients)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Row {
                        IconButton(onClick = { cameraLauncher.launch(null) }) {
                            Icon(imageVector = Icons.Rounded.PhotoCamera, contentDescription = "Caméra")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            try {
                                speechLauncher.launch(speechIntent)
                            } catch (_: ActivityNotFoundException) {
                                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_speech_unavailable)) }
                            }
                        }) {
                            Icon(imageVector = Icons.Filled.Mic, contentDescription = stringResource(R.string.action_dictate))
                        }
                    }
                }
            )

            val restrictionOptions = remember {
                listOf(
                    context.getString(R.string.filter_vegetarian),
                    context.getString(R.string.filter_gluten_free),
                    context.getString(R.string.filter_spicy)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.title_restrictions),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
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
                            label = { Text(opt) },
                            leadingIcon = if (selected) {
                                { Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }

            val vibes = remember {
                listOf(
                    context.getString(R.string.vibe_fast),
                    context.getString(R.string.vibe_gourmet),
                    context.getString(R.string.vibe_fun)
                )
            }
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

            // Galerie d’images capturées (au-dessus du bouton Cuisiner)
            if (capturedImages.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(capturedImages) { bmp ->
                        Box(modifier = Modifier.size(100.dp)) {
                            Card(
                                modifier = Modifier.matchParentSize(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Photo capturée",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            IconButton(
                                onClick = { capturedImages.remove(bmp) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    keyboardController?.hide()
                    val canGenerate = ingredients.isNotBlank() || capturedImages.isNotEmpty()
                    if (canGenerate) {
                        val ingredientsForCall = if (ingredients.isNotBlank()) ingredients else "Ingrédients détectés par l'IA"
                        viewModel.generateRecipe(
                            ingredientsForCall,
                            selectedVibe,
                            selectedFilters.toList(),
                            images = capturedImages.toList()
                        )
                    }
                },
                enabled = ingredients.isNotBlank() || capturedImages.isNotEmpty(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.btn_cook))
            }

            when (val state = uiState) {
                UiState.Initial -> {
                    Text(
                        text = stringResource(R.string.msg_initial_instructions),
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
                            animation = tween(durationMillis = 800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "iconScale"
                    )
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1200),
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
                                contentDescription = stringResource(R.string.desc_loading_icon),
                                modifier = Modifier.size(80.dp).scale(scale),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = context.getString(R.string.loading_thinking),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    val scrollState = rememberScrollState()
                    val lines = remember(state.recipe) { state.recipe.lines() }
                    val title: String = lines.firstOrNull { it.startsWith("# ") }?.removePrefix("# ")
                        ?.trim().orEmpty().ifBlank { stringResource(R.string.fallback_recipe_title) }
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
                                    viewModel.saveCurrentRecipe(userId)
                                    scope.launch { snackbarHostState.showSnackbar("Recette sauvegardée dans l'historique !") }
                                }) {
                                    Icon(imageVector = Icons.Filled.BookmarkAdd, contentDescription = "Ajouter aux favoris")
                                }
                                IconButton(onClick = {
                                    val clip = ClipData.newPlainText("recipe", state.recipe)
                                    clipboard.setPrimaryClip(clip)
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_recipe_copied)) }
                                }) {
                                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.action_copy))
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
                                contentDescription = stringResource(R.string.desc_error_icon),
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
                                    val canGenerate = ingredients.isNotBlank() || capturedImages.isNotEmpty()
                                    if (canGenerate) {
                                        val ingredientsForCall = if (ingredients.isNotBlank()) ingredients else "Ingrédients détectés par l'IA"
                                        viewModel.generateRecipe(
                                            ingredientsForCall,
                                            selectedVibe,
                                            selectedFilters.toList(),
                                            images = capturedImages.toList()
                                        )
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_retry_missing_ingredients)) }
                                    }
                                }
                            ) {
                                Text(text = stringResource(R.string.action_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}
