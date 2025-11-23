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
- Implémentation d’une génération de recettes via l’IA Gemini (Google Generative AI).
- Intégration de Firebase Firestore pour la sauvegarde et l’historique des recettes par utilisateur.
- Ajout d’une interface riche avec Jetpack Compose : boutons de génération, sauvegarde, historique, partage, copie, reconnaissance vocale et capture d’images.
- Fonction de dictée vocale permettant de saisir les ingrédients par la voix.
- Gestion de l’authentification (login / signup) avec Firebase Auth.
- Navigation complète (Login → Home → Historique → Détail) via Navigation‑Compose.
- Support du mode sombre, animations, icônes premium et design moderne.

### Comment l'IA a été ajoutée
1. *Dépendances* : dans gradle/libs.versions.toml nous avons ajouté :
   toml
   generative-ai = { group = "com.google.firebase", name = "generative-ai", version = "0.2.0" }
   coil-compose = { group = "io.coil-kt", name = "coil-compose", version = "2.5.0" }
   
   et dans app/build.gradle.kts :
   kotlin
   implementation(libs.generative.ai)
   implementation(libs.coil.compose)
   
2. *Repository IA* : création de GeminiRepository.kt qui construit le prompt, gère les filtres et accepte des images (generateRecipe(..., images: List<Bitmap>)).
       val id: String = "",
       val userId: String = "",
       val title: String = "",
       val content: String = "",
       val timestamp: Long = System.currentTimeMillis()
   )
   ```
   ```kotlin
   // Exemple d’appel depuis le ViewModel
   firestoreRepository.saveRecipe(userId, recipe)
   ```

Ces ajouts permettent à l’application VibeChef de générer des recettes créatives grâce à l’IA, de les sauvegarder et de les consulter ultérieurement, tout en offrant une expérience utilisateur moderne et fluide.

### Liens vers la vidéo

Voir [vidéo démonstrative](https://www.youtube.com/shorts/zgxqGORrfo8)

