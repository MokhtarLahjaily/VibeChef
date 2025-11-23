package fr.unica.fetheddine.lahjaily.vibechef.data

import android.graphics.Bitmap
import fr.unica.fetheddine.lahjaily.vibechef.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.content

class GeminiRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.API_KEY,
        generationConfig = generationConfig {
            temperature = 0.9f
        }
    )

    /**
     * Génère une recette à partir d'ingrédients et d'une ambiance.
     * Peut optionnellement utiliser plusieurs images pour détecter des ingrédients (multimodal).
     */
    suspend fun generateRecipe(
        ingredients: String,
        vibe: String,
        filters: List<String>,
        images: List<Bitmap> = emptyList()
    ): String {
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

        val intro = if (images.isNotEmpty()) {
            "Analyse attentivement ces images pour identifier tous les ingrédients visibles (légumes, viandes, etc.). Combine ces ingrédients visuels avec la liste textuelle suivante fournie par l'utilisateur : $ingredients."
        } else {
            "Tu es un chef cuisinier créatif. Crée une recette structurée en français avec ces ingrédients : $ingredients."
        }

        val prompt = """
            $intro
            Ambiance du repas : $vibe
            Restrictions / Contraintes: $restrictions
            
            ${if (constraintInstructions.isNotEmpty()) "Applique les règles suivantes pour les contraintes demandées :\n$constraintInstructions" else ""}
            
            Format de sortie attendu (Markdown) :
            # [Nom de la recette créative et amusante ici]

            ### 🍽️ Ingrédients
            - Liste des ingrédients avec quantités estimées (adapter selon restrictions)
            
            ### 🔥 Instructions
            1. Étapes numérotées claires et concises (intégrer les adaptations nécessaires)
            
            Ajoute des émojis pertinents au début de chaque grand titre (Ingrédients, Instructions) pour rendre la lecture plus amusante.
            Si un ingrédient semble incohérent avec une restriction active, ajoute une ligne **Note:** avant la section Ingrédients pour proposer une substitution.
            N'ajoute aucune autre section (pas d'intro ni de conclusion).
        """.trimIndent()

        val inputContent = content {
            images.forEach { image(it) }
            text(prompt)
        }
        val response = generativeModel.generateContent(inputContent)
        return response.text ?: throw Exception("Réponse vide de l'IA")
    }
}
