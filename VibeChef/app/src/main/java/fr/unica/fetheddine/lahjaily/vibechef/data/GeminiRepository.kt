package fr.unica.fetheddine.lahjaily.vibechef.data

import fr.unica.fetheddine.lahjaily.vibechef.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

class GeminiRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.API_KEY,
        generationConfig = generationConfig {
            temperature = 0.9f
        }
    )

    /**
     * Generates a recipe based on ingredients and a vibe.
     *
     * @param ingredients The ingredients for the recipe.
     * @param vibe The vibe for the meal.
     * @return The generated recipe as a String.
     */
    suspend fun generateRecipe(ingredients: String, vibe: String): String {
        val prompt = """
            Tu es un chef cuisinier créatif. Crée une recette avec ces ingrédients : $ingredients.
            L'ambiance du repas est : $vibe.
            Sois concis et formatte la réponse avec des titres clairs.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "La recette générée est vide."
        } catch (e: Exception) {
            // It's a good practice to log the exception
            e.printStackTrace()
            "Une erreur est survenue lors de la génération de la recette : ${e.message}"
        }
    }
}
