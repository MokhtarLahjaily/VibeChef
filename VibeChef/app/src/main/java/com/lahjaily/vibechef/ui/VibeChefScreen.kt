package com.lahjaily.vibechef.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lahjaily.vibechef.R
import com.lahjaily.vibechef.ui.components.*
import com.lahjaily.vibechef.ui.viewmodel.MainViewModel
import com.lahjaily.vibechef.ui.viewmodel.UiState
import kotlinx.coroutines.launch
import java.util.Locale

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
    val isDark by viewModel.isDarkMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Voice recognition  appends to existing ingredients
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                ingredients = if (ingredients.isBlank()) spokenText
                else "$ingredients, $spokenText"
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

    // Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) capturedImages.add(bitmap)
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                        .copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                capturedImages.add(bitmap)
            } catch (_: Exception) { }
        }
    }

    val restrictionOptions = remember {
        listOf(
            context.getString(R.string.filter_vegetarian),
            context.getString(R.string.filter_gluten_free),
            context.getString(R.string.filter_spicy)
        )
    }
    val vibes = remember {
        listOf(
            context.getString(R.string.vibe_fast),
            context.getString(R.string.vibe_gourmet),
            context.getString(R.string.vibe_fun)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_app)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconToggleButton(checked = isDark, onCheckedChange = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                            contentDescription = if (isDark) stringResource(R.string.desc_theme_toggle_light) else stringResource(R.string.desc_theme_toggle_dark)
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(imageVector = Icons.Filled.History, contentDescription = stringResource(R.string.action_history))
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.action_logout))
                    }
                }
            )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ingredients input
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text(stringResource(R.string.label_ingredients)) },
                placeholder = { Text(stringResource(R.string.placeholder_ingredients)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                trailingIcon = {
                    Row {
                        IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = stringResource(R.string.action_gallery))
                        }
                        IconButton(onClick = { cameraLauncher.launch(null) }) {
                            Icon(imageVector = Icons.Rounded.PhotoCamera, contentDescription = stringResource(R.string.action_camera))
                        }
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

            // Filters & vibes
            FiltersSection(
                restrictionOptions = restrictionOptions,
                selectedFilters = selectedFilters,
                onFilterToggle = { opt ->
                    selectedFilters = if (opt in selectedFilters) selectedFilters - opt else selectedFilters + opt
                }
            )

            VibeSelector(
                vibes = vibes,
                selectedVibe = selectedVibe,
                onVibeSelected = { selectedVibe = it }
            )

            // Captured images
            CapturedImagesGallery(
                images = capturedImages.toList(),
                onRemove = { capturedImages.remove(it) }
            )

            // Cook button
            Button(
                onClick = {
                    keyboardController?.hide()
                    val canGenerate = ingredients.isNotBlank() || capturedImages.isNotEmpty()
                    if (canGenerate) {
                        val ingredientsForCall = if (ingredients.isNotBlank()) ingredients else "Ingredients detected by AI"
                        viewModel.generateRecipe(
                            ingredientsForCall,
                            selectedVibe,
                            selectedFilters.toList(),
                            images = capturedImages.toList()
                        )
                    }
                },
                enabled = ingredients.isNotBlank() || capturedImages.isNotEmpty(),
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_cook))
            }

            // State display
            when (val state = uiState) {
                UiState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.msg_initial_instructions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                UiState.Loading -> {
                    LoadingState(modifier = Modifier.weight(1f))
                }

                is UiState.Success -> {
                    RecipeCard(
                        recipe = state.recipe,
                        onSave = {
                            viewModel.saveCurrentRecipe(userId)
                            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_recipe_saved)) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = {
                            val canGenerate = ingredients.isNotBlank() || capturedImages.isNotEmpty()
                            if (canGenerate) {
                                val ingredientsForCall = if (ingredients.isNotBlank()) ingredients else "Ingredients detected by AI"
                                viewModel.generateRecipe(
                                    ingredientsForCall,
                                    selectedVibe,
                                    selectedFilters.toList(),
                                    images = capturedImages.toList()
                                )
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.snackbar_retry_missing_ingredients)) }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
