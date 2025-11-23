package fr.unica.fetheddine.lahjaily.vibechef

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.API_KEY
    )
) : ViewModel() {

    private val _uiState: MutableStateFlow<RecipeUiState> =
        MutableStateFlow(RecipeUiState.Initial)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    fun generateRecipe(prompt: String) {
        _uiState.value = RecipeUiState.Loading

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(
                    content { text(prompt) }
                )
                response.text?.let {
                    _uiState.value = RecipeUiState.Success(it)
                } ?: run {
                    _uiState.value = RecipeUiState.Error("No recipe generated.")
                }
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}

sealed interface RecipeUiState {
    object Initial : RecipeUiState
    object Loading : RecipeUiState
    data class Success(val output: String) : RecipeUiState
    data class Error(val error: String) : RecipeUiState
}
