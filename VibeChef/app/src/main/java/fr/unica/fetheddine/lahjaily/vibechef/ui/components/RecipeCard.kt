package fr.unica.fetheddine.lahjaily.vibechef.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun RecipeCard(
    recipe: String,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val clipboard: ClipboardManager = remember(context) {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    val lines = remember(recipe) { recipe.lines() }
    val title: String = lines.firstOrNull { it.startsWith("# ") }?.removePrefix("# ")
        ?.trim().orEmpty().ifBlank { "Recette" }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    onSave()
                }) {
                    Icon(
                        imageVector = Icons.Filled.BookmarkAdd,
                        contentDescription = "Sauvegarder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    val clip = ClipData.newPlainText("recipe", recipe)
                    clipboard.setPrimaryClip(clip)
                    scope.launch { snackbarHostState.showSnackbar("Recette copiée !") }
                }) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copier"
                    )
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, recipe)
                    }
                    context.startActivity(Intent.createChooser(intent, "Partager la recette"))
                }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Partager"
                    )
                }
            }
            MarkdownText(text = recipe)
        }
    }
}
