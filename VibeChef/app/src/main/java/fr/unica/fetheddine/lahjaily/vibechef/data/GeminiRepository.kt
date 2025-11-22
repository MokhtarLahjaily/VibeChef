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
            Tu es un chef cuisinier créatif.
            Crée une recette structurée en français avec ces ingrédients : $ingredients
            Ambiance du repas : $vibe
            
            Format de sortie attendu (Markdown) :
            ### 🍽️ Ingrédients
            - Liste des ingrédients avec quantités estimées.
            
            ### 🔥 Instructions
            1. Étapes numérotées claires et concises.
            
            Ajoute des émojis pertinents au début de chaque grand titre (Ingrédients, Instructions) pour rendre la lecture plus amusante (ex: 🍅🥕🔥🍽️👨‍🍳). Garde la structure claire et concise.
            Si un ingrédient semble incohérent, ajoute une ligne **Note:** avant la section Ingrédients.
            N'ajoute aucune autre section.
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
