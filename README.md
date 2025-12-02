[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/TIr6ybvI)
# Université Côte d'Azur - DS4H - EMSI - IA2
## Cours de Programmation Mobile et IA - Leo Donati
## TD3 - Soyez créatif avec l'IA

Dans ce TD, vous pouvez choisir quelle technologie vous voulez utiliser pour ajouter de l'IA à votre application. Vous pouvez utiliser des modèles pré-entrainés, des API ou créer votre propre modèle.

Ce TD est à faire en groupe de 2 ou 3 personnes.

### Membres du groupe
- Aya Fetheddine
- Mohamed Mokhtar Lahjaily

### Objectifs atteints
- Implémentation d’une génération de recettes via l’API **Gemini** (Google Generative AI).
- Intégration de **Firebase Firestore** pour la sauvegarde et l’historique des recettes par utilisateur.
- Interface moderne avec **Jetpack Compose** : génération de recettes, sauvegarde, historique, partage et copie dans le presse-papiers.
- Fonction de **dictée vocale** (Speech-to-Text) permettant de saisir les ingrédients par la voix.
- Gestion de l’authentification (Connexion / Inscription) avec **Firebase Auth**.
- Navigation fluide (Login ↔ Home ↔ Historique) via **Navigation-Compose**.
- Support du mode sombre (Dark Mode) et gestion des états d'UI (Loading, Success, Error).

### Comment l'IA a été ajoutée

1. **Dépendances** : 
   Dans `gradle/libs.versions.toml`, nous avons ajouté le SDK Google AI Client :
   ```toml
   generative-ai = { group = "com.google.ai.client.generativeai", name = "generativeai", version.ref = "generativeai" }
   ```
   et dans app/build.gradle.kts :
   ```kotlin
   implementation(libs.generative.ai)
   ```
   
2. *Repository IA* Repository IA (GeminiRepository.kt) : Nous avons créé une classe qui configure le modèle gemini-2.5-flash. La fonction principale generateRecipe construit un prompt structuré incluant les ingrédients, l'ambiance ("Vibe") et les restrictions alimentaires, pour générer une réponse formatée en Markdown.

   ```kotlin
   suspend fun generateRecipe(ingredients: String, vibe: String, filters: List<String>): String {
    // Construction du prompt et appel à l'API
    val response = generativeModel.generateContent(prompt)
    return response.text ?: throw Exception("Réponse vide")
   }
   ```
 3. Intégration UI (MainViewModel.kt) : Le ViewModel appelle ce repository de manière asynchrone (Coroutines) et expose le résultat via un StateFlow pour que l'interface s'adapte automatiquement (chargement, affichage de la recette).
 
Ces ajouts permettent à l’application VibeChef de générer des recettes créatives grâce à l’IA, de les sauvegarder et de les consulter ultérieurement, tout en offrant une expérience utilisateur moderne et fluide.

### Liens vers la vidéo

Voir [vidéo démonstrative](https://www.youtube.com/watch?v=UJYe2OhJL50)

