package fr.unica.fetheddine.lahjaily.vibechef.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import fr.unica.fetheddine.lahjaily.vibechef.data.model.Recipe
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveRecipe(userId: String, recipe: Recipe) {
        val collection = db.collection("users").document(userId).collection("recipes")
        val docRef = if (recipe.id.isEmpty()) collection.document() else collection.document(recipe.id)
        val recipeToSave = recipe.copy(id = docRef.id, userId = userId)
        docRef.set(recipeToSave).await()
    }

    fun getRecipes(userId: String): Flow<List<Recipe>> = callbackFlow {
        val collection = db.collection("users").document(userId).collection("recipes")
        val query = collection.orderBy("timestamp", Query.Direction.DESCENDING)

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

    suspend fun deleteRecipe(userId: String, recipeId: String) {
        db.collection("users").document(userId).collection("recipes")
            .document(recipeId).delete().await()
    }
}
