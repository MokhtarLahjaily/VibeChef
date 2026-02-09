package com.lahjaily.vibechef.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE userId = :userId ORDER BY timestamp DESC")
    fun getRecipesByUser(userId: String): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteById(recipeId: String)

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavorite(recipeId: String, isFavorite: Boolean)

    @Query("DELETE FROM recipes WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
