package fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.fetheddine.lahjaily.vibechef.data.GeminiRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

// 1. Sealed interface pour représenter les états de l'UI
sealed interface UiState {
    data object Initial : UiState
    data object Loading : UiState
    data class Success(val recipe: String) : UiState
    data class Error(val message: String) : UiState
}

// 2. ViewModel pour orchestrer la logique
class MainViewModel(
    // Injection de dépendance pour le repository
    private val geminiRepository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    // 3. StateFlow privé et mutable pour gérer l'état en interne
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    // StateFlow public et immuable exposé à l'UI
    val uiState = _uiState.asStateFlow()

    /**
     * Lance la génération de recette via le repository avec timeout et logs.
     */
    fun generateRecipe(ingredients: String, vibe: String, filters: List<String> = emptyList()) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            Log.d("VibeChefDebug", "Début de la génération pour $ingredients | vibe=$vibe | filters=${filters.joinToString()}")
            try {
                val recipe = withTimeout(30_000L) {
                    geminiRepository.generateRecipe(ingredients, vibe, filters)
                }
                Log.d("VibeChefDebug", "Recette reçue !")
                _uiState.value = UiState.Success(recipe)
            } catch (e: TimeoutCancellationException) {
                Log.e("VibeChefDebug", "Timeout !")
                _uiState.value = UiState.Error("Le chef met trop de temps à répondre (Timeout). Vérifie ta connexion.")
            } catch (e: Exception) {
                Log.e("VibeChefDebug", "Erreur : ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }
}
