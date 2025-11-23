package fr.unica.fetheddine.lahjaily.vibechef.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import fr.unica.fetheddine.lahjaily.vibechef.data.model.Recipe
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecipeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")

    suspend fun saveRecipe(recipe: Recipe): Result<Unit> {
        return try {
            // Si l'ID est vide, on laisse Firestore en générer un
            val docRef = if (recipe.id.isEmpty()) {
                recipesCollection.document()
            } else {
                recipesCollection.document(recipe.id)
            }
            
            val recipeToSave = recipe.copy(id = docRef.id)
            docRef.set(recipeToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserRecipes(userId: String): Flow<List<Recipe>> = callbackFlow {
        val query = recipesCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val recipes = snapshot.toObjects(Recipe::class.java)
                trySend(recipes)
            }
        }

        awaitClose { subscription.remove() }
    }
}
