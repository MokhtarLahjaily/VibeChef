package com.lahjaily.vibechef.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lahjaily.vibechef.data.model.Recipe

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isFavorite: Boolean
) {
    fun toRecipe(): Recipe = Recipe(
        id = id,
        userId = userId,
        title = title,
        content = content,
        timestamp = timestamp,
        isFavorite = isFavorite
    )

    companion object {
        fun fromRecipe(recipe: Recipe): RecipeEntity = RecipeEntity(
            id = recipe.id,
            userId = recipe.userId,
            title = recipe.title,
            content = recipe.content,
            timestamp = recipe.timestamp,
            isFavorite = recipe.isFavorite
        )
    }
}
