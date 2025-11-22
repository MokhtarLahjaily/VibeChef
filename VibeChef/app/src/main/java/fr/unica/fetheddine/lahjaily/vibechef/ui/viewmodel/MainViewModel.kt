package fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.fetheddine.lahjaily.vibechef.data.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
     * Lance la génération de recette via le repository.
     * Met à jour l'UiState pour refléter le chargement, le succès ou l'erreur.
     */
    fun generateRecipe(ingredients: String, vibe: String, filters: List<String>) {
        // 4. Utilise viewModelScope pour lancer une coroutine liée au cycle de vie du ViewModel
        viewModelScope.launch {
            // Met l'état à Loading avant de commencer l'appel réseau
            _uiState.value = UiState.Loading
            try {
                // Appelle la fonction suspend du repository
                val recipe = geminiRepository.generateRecipe(ingredients, vibe, filters)
                // Met à jour l'état avec le résultat en cas de succès
                _uiState.value = UiState.Success(recipe)
            } catch (e: Exception) {
                // Met à jour l'état avec le message d'erreur en cas d'échec
                _uiState.value = UiState.Error(e.message ?: "Une erreur inconnue est survenue")
            }
        }
    }
}
