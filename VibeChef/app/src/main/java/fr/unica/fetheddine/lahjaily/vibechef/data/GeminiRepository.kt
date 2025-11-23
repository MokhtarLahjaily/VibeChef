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
     * @param filters The dietary restrictions or preferences.
     * @return The generated recipe as a String.
     */
    suspend fun generateRecipe(ingredients: String, vibe: String, filters: List<String>): String {
        val restrictions = if (filters.isNotEmpty()) filters.joinToString(", ") else "Aucune"
        val constraintInstructions = StringBuilder()
        if (filters.any { it.contains("Végétarien", ignoreCase = true) }) {
            constraintInstructions.append("- \"Végétarien\" => aucune viande ou poisson\n")
        }
        if (filters.any { it.contains("Gluten", ignoreCase = true) }) {
            constraintInstructions.append("- \"Sans Gluten\" => éviter blé, seigle, orge; proposer alternatives (riz, maïs, avoine certifiée, etc.)\n")
        }
        if (filters.any { it.contains("Épicé", ignoreCase = true) }) {
            constraintInstructions.append("- \"Épicé\" => ajouter une chaleur modérée (piment, paprika fumé, piment d'Espelette) sans masquer les saveurs\n")
        }

        val prompt = """
            Tu es un chef cuisinier créatif.
            Crée une recette structurée en français avec ces ingrédients : $ingredients
            Ambiance du repas : $vibe
            Restrictions / Contraintes: $restrictions
            
            ${if (constraintInstructions.isNotEmpty()) "Applique les règles suivantes pour les contraintes demandées :\n$constraintInstructions" else ""}
            
            Format de sortie attendu (Markdown) :
            ### 🍽️ Ingrédients
            - Liste des ingrédients avec quantités estimées (adapter selon restrictions)
            
            ### 🔥 Instructions
            1. Étapes numérotées claires et concises (intégrer les adaptations nécessaires)
            
            Ajoute des émojis pertinents au début de chaque grand titre (Ingrédients, Instructions) pour rendre la lecture plus amusante.
            Si un ingrédient semble incohérent avec une restriction active, ajoute une ligne **Note:** avant la section Ingrédients pour proposer une substitution.
            N'ajoute aucune autre section.
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        return response.text ?: throw Exception("Réponse vide de l'IA")
    }
}
