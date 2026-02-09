package com.lahjaily.vibechef.ui.viewmodel

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lahjaily.vibechef.data.GeminiRepository
import com.lahjaily.vibechef.data.local.RecipeDao
import com.lahjaily.vibechef.data.local.RecipeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import com.lahjaily.vibechef.data.FirestoreRepository
import com.lahjaily.vibechef.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// 1. Sealed interface pour representer les etats de l'UI
sealed interface UiState {
    data object Initial : UiState
    data object Loading : UiState
    data class Success(val recipe: String) : UiState
    data class Error(val message: String) : UiState
}

// 2. ViewModel pour orchestrer la logique
@HiltViewModel
class MainViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val firestoreRepository: FirestoreRepository,
    private val recipeDao: RecipeDao,
    private val prefs: SharedPreferences
) : ViewModel() {

    // 3. StateFlow prive et mutable pour gerer l'etat en interne
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe = _selectedRecipe.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode = _isDarkMode.asStateFlow()

    val hasSeenOnboarding: Boolean
        get() = prefs.getBoolean("has_seen_onboarding", false)

    fun markOnboardingSeen() {
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
    }

    /**
     * Lance la generation de recette via le repository avec timeout et logs.
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
                "Debut de la generation pour $ingredients | vibe=$vibe | filters=${filters.joinToString()} | images=${images.size}"
            )
            try {
                val recipeContent = withTimeout(30_000L) {
                    geminiRepository.generateRecipe(ingredients, vibe, filters, images)
                }
                Log.d("VibeChefDebug", "Recette recue !")
                _uiState.value = UiState.Success(recipeContent)
            } catch (e: TimeoutCancellationException) {
                Log.e("VibeChefDebug", "Timeout !")
                _uiState.value = UiState.Error("Le chef met trop de temps a repondre (Timeout). Verifie ta connexion.")
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
                    val content = currentState.recipe
                    val title = content.lines().firstOrNull { it.startsWith("# ") }
                        ?.removePrefix("# ")?.trim() ?: "Recette VibeChef"

                    val recipe = Recipe(
                        userId = userId,
                        title = title,
                        content = content
                    )
                    firestoreRepository.saveRecipe(userId, recipe)
                    Log.d("VibeChefDebug", "Recette sauvegardee pour $userId")
                } catch (e: Exception) {
                    Log.e("VibeChefDebug", "Erreur sauvegarde : ${e.message}")
                }
            }
        }
    }

    /**
     * Returns recipe history. Firestore is the primary source;
     * recipes are cached locally in Room for offline access.
     */
    fun getUserHistory(userId: String): Flow<List<Recipe>> {
        return firestoreRepository.getRecipes(userId)
            .onEach { recipes ->
                // Cache to Room on each Firestore update
                recipeDao.insertAll(recipes.map { RecipeEntity.fromRecipe(it) })
            }
            .catch {
                // On error (offline), fallback to local Room cache
                Log.d("VibeChefDebug", "Firestore indisponible, fallback Room")
                emitAll(recipeDao.getRecipesByUser(userId).map { entities ->
                    entities.map { it.toRecipe() }
                })
            }
    }

    fun selectRecipe(recipe: Recipe) { _selectedRecipe.value = recipe }

    fun deleteRecipe(userId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                firestoreRepository.deleteRecipe(userId, recipeId)
                recipeDao.deleteById(recipeId)
                Log.d("VibeChefDebug", "Recette supprimee: $recipeId")
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

    fun toggleFavorite(userId: String, recipeId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                val newValue = !isFavorite
                firestoreRepository.toggleFavorite(userId, recipeId, newValue)
                recipeDao.updateFavorite(recipeId, newValue)
                Log.d("VibeChefDebug", "Favori toggled: $recipeId -> $newValue")
            } catch (e: Exception) {
                Log.e("VibeChefDebug", "Erreur favori : ${e.message}")
            }
        }
    }
}