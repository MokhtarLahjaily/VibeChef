package com.lahjaily.vibechef.data.model

data class Recipe(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    // Constructeur vide requis par Firestore pour la désérialisation
    constructor() : this("", "", "", "", System.currentTimeMillis(), false)
}
