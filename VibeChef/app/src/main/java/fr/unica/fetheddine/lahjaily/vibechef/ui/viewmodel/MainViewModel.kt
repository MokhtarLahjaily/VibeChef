package fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.fetheddine.lahjaily.vibechef.data.GeminiRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import fr.unica.fetheddine.lahjaily.vibechef.data.FirestoreRepository
import fr.unica.fetheddine.lahjaily.vibechef.data.model.Recipe
import kotlinx.coroutines.flow.update

// 1. Sealed interface pour représenter les états de l'UI
sealed interface UiState {
    data object Initial : UiState
    data object Loading : UiState
    data class Success(val recipe: String) : UiState
    data class Error(val message: String) : UiState
}

// 2. ViewModel pour orchestrer la logique
class MainViewModel(
    application: Application,
    private val geminiRepository: GeminiRepository = GeminiRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("vibechef_prefs", 0)

    // 3. StateFlow privé et mutable pour gérer l'état en interne
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode = _isDarkMode.asStateFlow()

    /**
     * Lance la génération de recette via le repository avec timeout et logs.
     */
    fun generateRecipe(
        ingredients: String,
        vibe: String,
        filters: List<String> = emptyList(),
        images: List<Bitmap> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            Log.d(
                "VibeChefDebug",
                "Début de la génération pour $ingredients | vibe=$vibe | filters=${filters.joinToString()} | images=${images.size}"
            )
            try {
                val recipeContent = withTimeout(30_000L) {
                    geminiRepository.generateRecipe(ingredients, vibe, filters, images)
                }
                Log.d("VibeChefDebug", "Recette reçue !")
                _uiState.value = UiState.Success(recipeContent)
            } catch (e: TimeoutCancellationException) {
                Log.e("VibeChefDebug", "Timeout !")
                _uiState.value = UiState.Error("Le chef met trop de temps à répondre (Timeout). Vérifie ta connexion.")
            } catch (e: Exception) {
                Log.e("VibeChefDebug", "Erreur : ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    fun saveCurrentRecipe(userId: String) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            viewModelScope.launch {
                try {
                    // Extraction basique du titre (première ligne commençant par #)
                    val content = currentState.recipe
                    val title = content.lines().firstOrNull { it.startsWith("# ") }
                        ?.removePrefix("# ")?.trim() ?: "Recette VibeChef"
                    
                    val recipe = Recipe(
                        userId = userId,
                        title = title,
                        content = content
                    )
                    firestoreRepository.saveRecipe(userId, recipe)
                    Log.d("VibeChefDebug", "Recette sauvegardée pour $userId")
                } catch (e: Exception) {
                    Log.e("VibeChefDebug", "Erreur sauvegarde : ${e.message}")
                }
            }
        }
    }

    fun getUserHistory(userId: String) = firestoreRepository.getRecipes(userId)

    fun selectRecipe(recipe: Recipe) { _selectedRecipe.value = recipe }

    fun deleteRecipe(userId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                firestoreRepository.deleteRecipe(userId, recipeId)
                Log.d("VibeChefDebug", "Recette supprimée: $recipeId")
            } catch (e: Exception) {
                Log.e("VibeChefDebug", "Erreur suppression : ${e.message}")
            }
        }
    }

    fun toggleTheme() {
        _isDarkMode.update { current ->
            val newValue = !current
            prefs.edit().putBoolean("dark_mode", newValue).apply()
            newValue
        }
    }
}
